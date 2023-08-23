import javax.xml.soap.*;

public class DeviceService extends Service {
	private DeviceService(String deviceServiceAddress, String u, String p) {
		endpoint = deviceServiceAddress;
		namespace = "http://www.onvif.org/ver10/device/wsdl";
		username = u;
		password = p;
		authenticate = false;
	}

	public static DeviceService getDeviceService(String deviceServiceAddress, String u, String p) {
		return new DeviceService(deviceServiceAddress, u, p);
	}

	private SOAPMessage getServicesMessage() throws SOAPException {
		SOAPMessage msg = super.getBaseMessage();
		SOAPBody body = msg.getSOAPBody();
		SOAPElement elem = body.addChildElement("GetServices", "wsdl");
		SOAPElement elem1 = elem.addChildElement("IncludeCapability", "wsdl");
		elem1.addTextNode("true");

		msg.saveChanges();
		return msg;
	}

	public String getServices() throws SOAPException {
		SOAPMessage msg = getServicesMessage();
		return sendRequest(msg);
	}

	private SOAPMessage getServiceCapabilitiesMessage() throws SOAPException {
		SOAPMessage msg = super.getBaseMessage();
		SOAPBody body = msg.getSOAPBody();
		SOAPElement elem = body.addChildElement("GetServiceCapabilities", "wsdl");

		msg.saveChanges();
		return msg;
	}

	public String getServiceCapabilities() throws SOAPException {
		SOAPMessage msg = getServicesMessage();
		return sendRequest(msg);
	}
}
