import javax.xml.soap.*;
import javax.xml.namespace.QName;

public class PTZService extends Service {
	private PTZService(String ptzServiceAddress, String u, String p) {
		endpoint = ptzServiceAddress;
		namespace = "http://www.onvif.org/ver20/ptz/wsdl";
		username = u;
		password = p;
		authenticate = true;
	}

	public static PTZService getPTZService(String ptzServiceAddress, String u, String p) {
		return new PTZService(ptzServiceAddress, u, p);
	}

	private SOAPMessage getConfigurationsMessage() throws SOAPException {
		SOAPMessage msg = getBaseMessage();
		SOAPBody body = msg.getSOAPBody();
		SOAPElement elem = body.addChildElement("GetConfigurations", "wsdl");

		msg.saveChanges();
		return msg;
	}

	public String getConfigurations() throws SOAPException {
		SOAPMessage msg = getConfigurationsMessage();
		return sendRequest(msg);
	}

	private SOAPMessage getConfigurationOptionsMessage(String cToken) throws SOAPException {
		SOAPMessage msg = getBaseMessage();
		SOAPBody body = msg.getSOAPBody();
		SOAPElement configurationOptions = body.addChildElement("GetConfigurationOptions", "wsdl");
		SOAPElement configurationToken = configurationOptions.addChildElement("ConfigurationToken", "wsdl");
		configurationToken.addTextNode(cToken);

		msg.saveChanges();
		return msg;
	}

	public String getConfigurationOptions(String cToken) throws SOAPException {
		SOAPMessage msg = getConfigurationOptionsMessage(cToken);
		return sendRequest(msg);
	}

	private SOAPMessage getContinuousMoveMessage(float xVel, float yVel) throws SOAPException {
		SOAPMessage msg = getBaseMessage();
		SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
		env.addNamespaceDeclaration("tt", "http://www.onvif.org/ver10/schema");
		SOAPBody body = msg.getSOAPBody();

		SOAPElement continuousMove = body.addChildElement("ContinuousMove", "wsdl");
		SOAPElement profileToken = continuousMove.addChildElement("ProfileToken", "wsdl");
		profileToken.addTextNode("Profile1");

		SOAPElement velocity = continuousMove.addChildElement("Velocity", "wsdl");
		SOAPElement panTilt = velocity.addChildElement("PanTilt", "tt");
		panTilt.addAttribute(new QName("x"), String.valueOf(xVel));
		panTilt.addAttribute(new QName("y"), String.valueOf(yVel));
		panTilt.addAttribute(new QName("space"), "http://www.onvif.org/ver10/tptz/PanTiltSpaces/VelocityGenericSpace");

		msg.saveChanges();
		return msg;
	}

	public String continuousMove(float x, float y) throws SOAPException {
		SOAPMessage msg = getContinuousMoveMessage(x, y);
		return sendRequest(msg);
	}

	private SOAPMessage getStopMessage() throws SOAPException {
		SOAPMessage msg = getBaseMessage();
		SOAPBody body = msg.getSOAPBody();
		SOAPElement stop = body.addChildElement("Stop", "wsdl");
		SOAPElement profileToken = stop.addChildElement("ProfileToken", "wsdl");
		profileToken.addTextNode("Profile1");
		SOAPElement panTilt = stop.addChildElement("PanTilt", "wsdl");
		panTilt.addTextNode("true");
		SOAPElement zoom = stop.addChildElement("Zoom", "wsdl");
		zoom.addTextNode("true");

		msg.saveChanges();
		return msg;
	}

	public String stop() throws SOAPException {
		SOAPMessage msg = getStopMessage();
		return sendRequest(msg);
	}
}
