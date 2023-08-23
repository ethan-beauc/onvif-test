import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Random;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import javax.xml.namespace.QName;

import javax.xml.soap.*;

public class ONVIFTest {
	public static void main(String[] args) throws Exception {
//		try {
//			Discovery.discoverONVIFDevices();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		Console c = System.console();
		String username = "", password = "";
		if (c != null) {
			username = c.readLine("Username: ");
			password = new String(c.readPassword("Password: "));
		}

		if ("".equals(username)) password = "";


		String url = "http://192.168.1.150/onvif/device_service";
		DeviceService deviceService = DeviceService.getDeviceService(url, username, password);
		System.out.println("\nServices: \n" + deviceService.getServices() + "\n");
		System.out.println("\nServiceCapabilities: \n" + deviceService.getServiceCapabilities() + "\n");

		url = "http://192.168.1.150:80/onvif/media";
		MediaService mediaService = MediaService.getMediaService(url, username, password);
		System.out.println("\nMedia Profiles: \n" + mediaService.getProfiles() + "\n");

		url = "http://192.168.1.150:80/onvif/ptz";
		PTZService ptzService = PTZService.getPTZService(url, username, password);
		System.out.println("\nConfigurations: \n" + ptzService.getConfigurations() + "\n");
		System.out.println("\nConfigurationOptions: \n" + ptzService.getConfigurationOptions("HD") + "\n");

		String userCommand = "";
		String resp = "";
		while (!"exit".equals(userCommand = c.readLine("Enter a command <exit|pantilt x y|stop>: "))) {
			String[] cmd = userCommand.split(" ");
			if (cmd.length == 0) continue;
			SOAPMessage msg = null;
			switch (cmd[0]) {
				case "pantilt":
					//url = "http://192.168.1.150:80/onvif/ptz";
					if (cmd.length == 3)
						resp = ptzService.continuousMove(Float.parseFloat(cmd[1]), Float.parseFloat(cmd[2]));
						//msg = getContinuousMoveMessage(Float.parseFloat(cmd[1]), Float.parseFloat(cmd[2]));
					else continue;
					break;
				case "stop":
					//url = "http://192.168.1.150:80/onvif/ptz";
					//msg = getStopMoveMessage();
					resp = ptzService.stop();
					break;
				//case "focus":
				//	url = "http://192.168.1.150:80/onvif/imaging";
				//	if (cmd.length == 2)
				//		msg = getFocusMessage(cmd[1]);
				//	else continue;
				//	break;
			}
			System.out.println("\n" + userCommand + ":\n" + resp + "\n");
		}
		System.out.println("Stopping camera...");
		ptzService.stop();
		System.out.println("Exiting...");
	}
}

