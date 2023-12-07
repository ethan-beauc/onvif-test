import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ImagingService extends Service {
	private ImagingService(String imagingServiceAddress, String u, String p) {
		endpoint = imagingServiceAddress;
		namespace = "http://www.onvif.org/ver20/imaging/wsdl";
		username = u;
		password = p;
		authenticate = true;
	}

	public static ImagingService getImagingService(String imagingServiceAddress, String u, String p) {
		return new ImagingService(imagingServiceAddress, u, p);
	}

	private Document getOptionsDocument(String vToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getOptions = doc.createElement("wsdl:GetOptions");
		body.appendChild(getOptions);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		getOptions.appendChild(videoSourceToken);

		return doc;
	}

	public String getOptions(String vToken) {
		Document doc = getOptionsDocument(vToken);
		return sendRequestDocument(doc);
	}

	private Document getImagingSettingsDocument(String vToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getImagingSettings = doc.createElement("wsdl:GetImagingSettings");
		body.appendChild(getImagingSettings);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		getImagingSettings.appendChild(videoSourceToken);

		return doc;
	}

	public String getImagingSettings(String vToken) {
		Document doc = getImagingSettingsDocument(vToken);
		return sendRequestDocument(doc);
	}

	private Document setImagingSettingsDocument(String vToken, String setting, String value) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element setImagingSettings = doc.createElement("wsdl:SetImagingSettings");
		body.appendChild(setImagingSettings);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		setImagingSettings.appendChild(videoSourceToken);

		Element imagingSettings = doc.createElement("wsdl:ImagingSettings");
		setImagingSettings.appendChild(imagingSettings);

		// Add more as implemented:
		switch (setting) {
			case "focus":
				Element focus = doc.createElement("tt:Focus");
				imagingSettings.appendChild(focus);

				Element afMode = doc.createElement("tt:AFMode");
				focus.appendChild(afMode);

				Element autoFocusMode = doc.createElement("tt:AutoFocusMode");
				focus.appendChild(autoFocusMode);
				if (!"auto".equalsIgnoreCase(value)) {
					autoFocusMode.appendChild(doc.createTextNode("MANUAL"));
					// only allows options for autofocus
				} else {
					autoFocusMode.appendChild(doc.createTextNode("AUTO"));
				}
				break;
			case "iris":
				Element exposure = doc.createElement("tt:Exposure");
				imagingSettings.appendChild(exposure);
				if (!"auto".equalsIgnoreCase(value) && !"manual".equalsIgnoreCase(value)) {
					Element mode = doc.createElement("tt:Mode");
					mode.appendChild(doc.createTextNode("MANUAL"));
					exposure.appendChild(mode);
					// uncomment if necessary at some point:
					//Element eTime = doc.createElement("tt:ExposureTime");
					//eTime.appendChild(doc.createTextNode("0.0"));
					//exposure.appendChild(eTime);
					//Element gain = doc.createElement("tt:Gain");
					//gain.appendChild(doc.createTextNode("0.48"));
					//exposure.appendChild(gain);
					Element iris = doc.createElement("tt:Iris");
					iris.appendChild(doc.createTextNode(value));
					exposure.appendChild(iris);
				} else if ("auto".equalsIgnoreCase(value)) {
					Element mode = doc.createElement("tt:Mode");
					mode.appendChild(doc.createTextNode("AUTO"));
					exposure.appendChild(mode);
				} else {
					Element mode = doc.createElement("tt:Mode");
					mode.appendChild(doc.createTextNode("MANUAL"));
					exposure.appendChild(mode);
				}
				break;
		}

		//Element forcePersistence = doc.createElement("wsdl:ForcePersistence");
		//forcePersistence.appendChild(doc.createTextNode("true");
		//setImagingSettings.appendChild(forcePersistence);

		return doc;
	}

	public String setFocus(String vToken, String value) {
		Document doc = setImagingSettingsDocument(vToken, "focus", value);
		return sendRequestDocument(doc);
	}

	public String setIris(String vToken, String value) {
		Document doc = setImagingSettingsDocument(vToken, "iris", value);
		return sendRequestDocument(doc);
	}

	private Document getMoveOptionsDocument(String vToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getMoveOptions = doc.createElement("wsdl:GetMoveOptions");
		body.appendChild(getMoveOptions);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		getMoveOptions.appendChild(videoSourceToken);

		return doc;
	}

	public String getMoveOptions(String vToken) {
		Document doc = getMoveOptionsDocument(vToken);
		return sendRequestDocument(doc);
	}

	private Document getMoveDocument(String vToken, float distance) {
		return getMoveDocument(vToken, distance, "");
	}

	private Document getMoveDocument(String vToken, float distance, String mode) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element moveElement = doc.createElement("wsdl:Move");
		body.appendChild(moveElement);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		moveElement.appendChild(videoSourceToken);

		Element focusElement = doc.createElement("wsdl:Focus");
		moveElement.appendChild(focusElement);
		// TODO: add autoselection with GetMoveOptions/verify Relative capability
		switch (mode.toLowerCase()) {
			case "absolute":
				// TODO: use GetStatus to get current, then add distance
				// (only necessary if camera doesn't support relative)
				Element absolute = doc.createElement("tt:Absolute");
				focusElement.appendChild(absolute);
				Element position = doc.createElement("tt:Position");
				position.appendChild(doc.createTextNode(String.valueOf(distance)));
				absolute.appendChild(position);
				break;
			case "continuous":
				// Treat distance like speed
				Element continuous = doc.createElement("tt:Continuous");
				focusElement.appendChild(continuous);
				Element speed = doc.createElement("tt:Speed");
				speed.appendChild(doc.createTextNode(String.valueOf(distance)));
				continuous.appendChild(speed);
				break;
			case "relative":
			default:
				Element relative = doc.createElement("tt:Relative");
				focusElement.appendChild(relative);
				Element distanceElement = doc.createElement("tt:Distance");
				distanceElement.appendChild(doc.createTextNode(String.valueOf(distance)));
				relative.appendChild(distanceElement);
				break;
		}

		return doc;
	}

	public String moveFocus(String vToken, float distance) {
		Document doc = getMoveDocument(vToken, distance);
		return sendRequestDocument(doc);
	}

	private Document getStatusDocument(String vToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getStatusElement = doc.createElement("wsdl:GetStatus");
		body.appendChild(getStatusElement);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		getStatusElement.appendChild(videoSourceToken);

		return doc;
	}

	public String getStatus(String vToken) {
		Document doc = getStatusDocument(vToken);
		return sendRequestDocument(doc);
	}

	private Document getStopDocument(String vToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element stopElement = doc.createElement("wsdl:Stop");
		body.appendChild(stopElement);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		stopElement.appendChild(videoSourceToken);

		return doc;
	}

	public String stop(String vToken) {
		Document doc = getStopDocument(vToken);
		return sendRequestDocument(doc);
	}
}