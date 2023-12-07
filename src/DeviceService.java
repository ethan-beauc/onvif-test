import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

	private Document getServicesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getServices = doc.createElement("wsdl:GetServices");
		body.appendChild(getServices);

		Element includeCapability = doc.createElement("wsdl:IncludeCapability");
		getServices.appendChild(includeCapability);
		includeCapability.appendChild(doc.createTextNode("true"));

		return doc;
	}

	public String getServices() {
		Document doc = getServicesDocument();
		return sendRequestDocument(doc);
	}

	private Document getScopesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getScopes = doc.createElement("wsdl:GetScopes");
		body.appendChild(getScopes);

		return doc;
	}

	public String getScopes() {
		authenticate = true;
		Document doc = getScopesDocument();
		return sendRequestDocument(doc);
	}

	private Document getServiceCapabilitiesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getServiceCapabilities = doc.createElement("wsdl:GetServiceCapabilities");
		body.appendChild(getServiceCapabilities);

		return doc;
	}

	public String getServiceCapabilities() {
		Document doc = getServiceCapabilitiesDocument();
		return sendRequestDocument(doc);
	}

	private Document getCapabilitiesDocument(String cat) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getCapabilities = doc.createElement("wsdl:GetCapabilities");
		body.appendChild(getCapabilities);

		Element category = doc.createElement("wsdl:Category");
		getCapabilities.appendChild(category);
		category.appendChild(doc.createTextNode(cat));

		return doc;
	}

	public String getCapabilities(String category) {
		Document doc = getCapabilitiesDocument(category);
		return sendRequestDocument(doc);
	}

	private Document getAuxiliaryCommandDocument(String command, String state) {
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
