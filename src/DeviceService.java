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
package us.mn.state.dot.tms.server.comm.onvifptz.lib;

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Service for ONVIF device messages
 *
 * @author Ethan Beauclaire
 */
public class DeviceService extends Service {
	public DeviceService(String deviceServiceAddress, String u, String p) {
		endpoint = deviceServiceAddress;
		namespace = "http://www.onvif.org/ver10/device/wsdl";
		username = u;
		password = p;
		authenticate = false;
	}

	public static DeviceService getDeviceService(String deviceServiceAddress, String u, String p) {
		return new DeviceService(deviceServiceAddress, u, p);
	}

	/** Document builder function for GetServices */
	public Document getServicesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getServices = doc.createElement("wsdl:GetServices");
		body.appendChild(getServices);

		Element includeCapability = doc.createElement("wsdl:IncludeCapability");
		getServices.appendChild(includeCapability);
		includeCapability.appendChild(doc.createTextNode("true"));

		return doc;
	}

	/** Get information about services on the device, including service capabilities */
	public String getServices() {
		Document doc = getServicesDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetScopes */
	public Document getScopesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getScopes = doc.createElement("wsdl:GetScopes");
		body.appendChild(getScopes);

		return doc;
	}

	/** Get the scope parameters of the device */
	public String getScopes() {
		authenticate = true;
		Document doc = getScopesDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetServiceCapabilities */
	public Document getServiceCapabilitiesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getServiceCapabilities = doc.createElement("wsdl:GetServiceCapabilities");
		body.appendChild(getServiceCapabilities);

		return doc;
	}

	/** Get the capabilities of the device service */
	public String getServiceCapabilities() {
		Document doc = getServiceCapabilitiesDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for SendAuxiliaryCommand */
	public Document getAuxiliaryCommandDocument(String command, String state) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element sendAuxiliaryCommand = doc.createElement("wsdl:SendAuxiliaryCommand");
		body.appendChild(sendAuxiliaryCommand);

		Element auxiliaryCommand = doc.createElement("wsdl:AuxiliaryCommand");
		sendAuxiliaryCommand.appendChild(auxiliaryCommand);
		auxiliaryCommand.appendChild(doc.createTextNode("tt:" + command + "|" + state));

		return doc;
	}
}
