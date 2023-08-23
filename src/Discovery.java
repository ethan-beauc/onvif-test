import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.LinkedList;

import javax.xml.soap.*;

public class Discovery {

	// WS-Discovery message to search for ONVIF devices
	private static final String WS_DISCOVERY_MESSAGE =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<e:Envelope xmlns:e=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
			"xmlns:w=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\"\n" +
			"xmlns:d=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\"\n" +
			"xmlns:dn=\"http://www.onvif.org/ver10/network/wsdl\">\n" +
			"<e:Header>\n" +
			"<w:MessageID>uuid:5e2092f4-5a57-4556-9037-9387f5f72e7e</w:MessageID>\n" +
			"<w:To e:mustUnderstand=\"true\">urn:schemas-xmlsoap-org:ws:2005:04:discovery</w:To>\n" +
			"<w:Action a:mustUnderstand=\"true\">http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</w:Action>\n" +
			"</e:Header>\n" +
			"<e:Body>\n" +
			"<d:Probe>\n" +
			"<d:Types>dn:NetworkVideoTransmitter</d:Types>\n" +
			"</d:Probe>\n" +
			"</e:Body>\n" +
			"</e:Envelope>";

	// Multicast address for WS-Discovery
	private static final String WS_DISCOVERY_MULTICAST_ADDRESS = "239.255.255.250";
	private static final int WS_DISCOVERY_PORT = 3702;

	public static void main(String args[]) {
		try {
			List<String> devs = discoverONVIFDevices();
			List<String> xaddrs = getXAddrs(devs);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String> discoverONVIFDevices() throws IOException {
		System.out.println(WS_DISCOVERY_MULTICAST_ADDRESS);
		InetAddress mcastaddr = InetAddress.getByName(WS_DISCOVERY_MULTICAST_ADDRESS);
		System.out.println(WS_DISCOVERY_PORT);
		InetSocketAddress group = new InetSocketAddress(mcastaddr, WS_DISCOVERY_PORT);
		NetworkInterface netIf = NetworkInterface.getByName("enp0s25");
		MulticastSocket s = new MulticastSocket(WS_DISCOVERY_PORT);

		s.joinGroup(group, netIf);
		s.setSoTimeout(3000);

		byte[] requestData = WS_DISCOVERY_MESSAGE.getBytes();

		DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, mcastaddr, WS_DISCOVERY_PORT);
		s.send(requestPacket);

		// Receive the responses
		List<String> rsps = new LinkedList<>();
		while (true) {
			byte[] buffer = new byte[8192]; // Adjust the buffer size as needed
			DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
			try {
				s.receive(responsePacket);
				String responseMessage = new String(responsePacket.getData(), 0, responsePacket.getLength());
				// Process the response message
				System.out.println("Received response from: " + responsePacket.getAddress());
				System.out.println(responseMessage);
				rsps.add(responseMessage);
			} catch (SocketTimeoutException ignored) {
				// Timeout reached, exit the loop
				break;
			}
		}

		// Leave the multicast group and close the socket
		s.leaveGroup(mcastaddr);
		s.close();

		return rsps;
	}

	private static List<String> getXAddrs(List<String> devices) {
		List<String> xaddrs = new LinkedList<>();
		for (String s : devices) {
			String xaddr = "";
			xaddrs.add(xaddr);
		}
		return xaddrs;
	}
}
