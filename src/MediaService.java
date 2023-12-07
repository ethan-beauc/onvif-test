import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MediaService extends Service {
	private MediaService(String mediaServiceAddress, String u, String p) {
		endpoint = mediaServiceAddress;
		namespace = "http://www.onvif.org/ver10/media/wsdl";
		username = u;
		password = p;
		authenticate = true;
	}

	public static MediaService getMediaService(String mediaServiceAddress, String u, String p) {
		return new MediaService(mediaServiceAddress, u, p);
	}

	private Document getProfilesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getProfiles = doc.createElement("wsdl:GetProfiles");
		body.appendChild(getProfiles);

		return doc;
	}

	public String getProfiles() {
		Document doc = getProfilesDocument();
		return sendRequestDocument(doc);
	}

	private Document getVideoSourcesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getVideoSources = doc.createElement("wsdl:GetVideoSources");
		body.appendChild(getVideoSources);

		return doc;
	}

	public String getVideoSources() {
		Document doc = getVideoSourcesDocument();
		return sendRequestDocument(doc);
	}
}
