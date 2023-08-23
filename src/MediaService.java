import javax.xml.soap.*;

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

	private SOAPMessage getProfilesMessage() throws SOAPException {
		SOAPMessage msg = super.getBaseMessage();
		SOAPBody body = msg.getSOAPBody();
		SOAPElement getProfiles = body.addChildElement("GetProfiles", "wsdl");

		msg.saveChanges();
		return msg;
	}

	public String getProfiles() throws SOAPException {
		SOAPMessage msg = getProfilesMessage();
		return sendRequest(msg);
	}
}
