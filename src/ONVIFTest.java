/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2023  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.server.comm.onvifptz;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import javax.xml.namespace.QName;

import org.w3c.dom.*;

/**
 * Test class for ONVIF Standard implementation
 *
 * @author Ethan Beauclaire
 */
public class ONVIFTest {
	public static void main(String[] args) throws Exception {
		String url = args[0];
		Console c = System.console();
		String username = "", password = "";
		if (c != null) {
			username = c.readLine("Username: ");
			password = new String(c.readPassword("Password: "));
		}

		if ("".equals(username)) password = "";


		DeviceService deviceService = DeviceService.getDeviceService(
			"http://" + url + "/onvif/device_service", username, password);
		//String services = deviceService.getServices();
		//MediaService mediaService = MediaService.getMediaService(
		//	deviceService.getMediaBinding(services), username, password);
		//PTZService ptzService = PTZService.getPTZService(
		//	deviceService.getPTZBinding(services), username, password);
		//ImagingService imagingService = ImagingService.getImagingService(
		//	deviceService.getImagingBinding(services), username, password);
		String capabilities = deviceService.getCapabilities();
		System.out.println("Capabilities: " + capabilities);
		MediaService mediaService = MediaService.getMediaService(
			deviceService.getMediaBinding(capabilities), username, password);
		PTZService ptzService = PTZService.getPTZService(
			deviceService.getPTZBinding(capabilities), username, password);
		ImagingService imagingService = ImagingService.getImagingService(
			deviceService.getImagingBinding(capabilities), username, password);
		//MediaService mediaService = MediaService.getMediaService(
		//	"http://" + url + "/onvif/device_service", username, password);
		//PTZService ptzService = PTZService.getPTZService(
		//	"http://" + url + "/onvif/device_service", username, password);
		//ImagingService imagingService = ImagingService.getImagingService(
		//	"http://" + url + "/onvif/device_service", username, password);

		String mediaProfile = null, videoSource = null;
		int mediaWidth = 0, videoWidth = 0;  // to find maximum values

		// Should contain all necessary tokens
		Document getProfilesRes = DOMUtils.getDocument(mediaService.getProfiles());

		if (getProfilesRes != null) {
			NodeList profiles = getProfilesRes.getElementsByTagNameNS("*", "Profiles");
			for (int i = 0; i < profiles.getLength(); i++) {
				int mx = 0, vx = 0;
				Element profile = (Element) profiles.item(i);

				// get the video source and its width
				Element videoConfig = (Element) profile.getElementsByTagNameNS("*", "VideoSourceConfiguration").item(0);
				if (videoConfig == null) continue;  // we want a profile with a video source
				Element sourceToken = (Element) videoConfig.getElementsByTagNameNS("*", "SourceToken").item(0);
				Element bounds = (Element) videoConfig.getElementsByTagNameNS("*", "Bounds").item(0);
				vx = Integer.parseInt(bounds.getAttribute("width"));

				// get the video encoder and its width, if applicable; only for better profile selection
				Element encoderConfig = (Element) profile.getElementsByTagNameNS("*", "VideoEncoderConfiguration").item(0);
				if (encoderConfig != null) {
					Element widthElem = (Element) encoderConfig.getElementsByTagNameNS("*", "Width").item(0);
					mx = Integer.parseInt(widthElem.getTextContent());
				}

				// if video source bigger than current, replace
				if (vx >= videoWidth) {
					System.out.println("Video width larger. Setting videoSource...");
					videoSource = sourceToken.getTextContent();
					videoWidth = vx;
				}
				// replace media profile only if it's larger and the attached source is no smaller
				if (mx >= mediaWidth && vx >= videoWidth) {
					System.out.println("Both widths larger. Setting mediaProfile...");
					mediaProfile = profile.getAttribute("token");
					mediaWidth = mx;
				}
			}
		}

		if (mediaProfile != null)
			System.out.println("Set media profile: " + mediaProfile);
		if (videoSource != null)
			System.out.println("Set video source: " + videoSource);

		String userCommand = "";
		String resp = "";
		while (!"exit".equals(userCommand = c.readLine("Enter a command <exit|ptzconfigs|ptzconfigoptions|services|capabilities|moveoptions|scopes|profiles|con x y z|rel x y z|stop|configurefocus s|focus f|relf f|stopf|getfocus|iris f|isettings|wiper On|Off|setpreset i|gotopreset i|getpresets>: "))) {
			String[] cmd = userCommand.split(" ");
			if (cmd.length == 0) continue;
			switch (cmd[0]) {
				case "ptz":
				case "con":
					if (cmd.length == 4)
						resp = ptzService.continuousMove(mediaProfile, cmd[1], cmd[2], cmd[3]);
					else continue;
					break;
				case "rel":
					if (cmd.length == 4)
						resp = ptzService.relativeMove(mediaProfile, Float.parseFloat(cmd[1]), Float.parseFloat(cmd[2]), Float.parseFloat(cmd[3]));
					else continue;
					break;
				case "stop":
					resp = ptzService.stop(mediaProfile);
					break;
				case "getpresets":
					resp = ptzService.getPresets(mediaProfile);
					break;
				case "gotopreset":
					if (cmd.length == 2)
						resp = ptzService.gotoPreset(mediaProfile, cmd[1]);
					else continue;
					break;
				case "setpreset":
					if (cmd.length == 2)
						resp = ptzService.setPreset(mediaProfile, cmd[1]);
					else continue;
					break;
				case "getiris":
					resp = String.valueOf(imagingService.getIris(videoSource));
					break;
				case "inciris":
					if (cmd.length == 2)
						resp = imagingService.incrementIris(videoSource, cmd[1]);
					else continue;
					break;
				case "iris":
					if (cmd.length == 2)
						resp = imagingService.setIris(videoSource, cmd[1]);
					else continue;
					break;
				case "configurefocus":
					if (cmd.length == 2)
						imagingService.setFocus(videoSource, cmd[1]);
					else continue;
					break;
				case "focus":
					if (cmd.length == 2)
						imagingService.moveFocus(videoSource, Float.parseFloat(cmd[1]));
					else continue;
					break;
				case "relf":
					if (cmd.length == 2)
						imagingService.moveFocus(videoSource, Float.parseFloat(cmd[1]), "relative");
					else continue;
					break;
				case "stopf":
					imagingService.stop(videoSource);
					break;
				case "getfocusoptions":
				case "moveoptions":
					resp = imagingService.getMoveOptions(videoSource);
					break;
				case "getfocus":
					resp = imagingService.getStatus(videoSource);
					break;
				case "nodes":
					resp = ptzService.getNodes();
					break;
				case "node":
					if (cmd.length >= 2)
						resp = ptzService.getNode(Arrays.asList(cmd).stream().skip(1).collect(Collectors.joining(" ")));
					else continue;
					break;
				case "scopes":
					resp = deviceService.getScopes();
					break;
				case "profiles":
					resp = mediaService.getProfiles();
					break;
				case "isettings":
					resp = imagingService.getImagingSettings(videoSource);
					break;
				case "ptzconfigs":
					resp = ptzService.getConfigurations();
					break;
				case "ptzconfigoptions":
					resp = ptzService.getConfigurationOptions("HD");
					break;
				case "services":
					resp = deviceService.getServices();
					break;
				case "capabilities":
					resp = deviceService.getCapabilities();
					break;
				case "wiper":
					if (cmd.length == 2)
						resp = ptzService.setWiper(mediaProfile, cmd[1]);
					break;
				case "wipe":
					resp = ptzService.wiperOneshot(mediaProfile);
					break;
			}
			System.out.println("\n" + userCommand + " response:\n" + resp + "\n");
		}
		System.out.println("Stopping camera...");
		imagingService.stop(videoSource);
		ptzService.stop(mediaProfile);
		System.out.println("Exiting...");
	}
}

