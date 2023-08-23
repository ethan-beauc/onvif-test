import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Random;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import javax.xml.soap.*;
import javax.xml.namespace.QName;

public abstract class Service {
	protected String endpoint;
	protected String namespace;
	protected String username;
	protected String password;
	protected boolean authenticate;

	protected SOAPMessage getBaseMessage() throws SOAPException {
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage message = factory.createMessage();
		SOAPPart part = message.getSOAPPart();

		SOAPEnvelope envelope = part.getEnvelope();
		envelope.addNamespaceDeclaration("wsdl", namespace);

		message.saveChanges();
		return message;
	}

	protected String buildRequest(SOAPMessage msg) throws SOAPException, IOException {
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		msg.writeTo(outstream);
		String req = new String(outstream.toByteArray());
		System.out.println("Built request:\n" + req);
		return req;
	}

	/**
	 * Generates a random 16-byte string for use as a nonce in password digest
	 */
	private static String getNonce() {
		Random random = new SecureRandom();
		StringBuilder nonce = new StringBuilder();

		char[] allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		for (int i = 0; i < 16; i++) {
			nonce.append(allowedChars[random.nextInt(allowedChars.length)]);
		}

		return nonce.toString();
	}

	private static String getUTCTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(new Date());
	}

	protected SOAPMessage addSecurityHeader(SOAPMessage msg) throws SOAPException, NoSuchAlgorithmException, UnsupportedEncodingException {
		SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
		env.addNamespaceDeclaration("wsse",
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		env.addNamespaceDeclaration("wsu",
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

		SOAPHeader head = env.getHeader();
		SOAPElement sec = head.addChildElement("Security", "wsse");
		SOAPElement usernameToken = sec.addChildElement("UsernameToken", "wsse");
		SOAPElement usernameElement = usernameToken.addChildElement("Username", "wsse");
		SOAPElement passwordElement = usernameToken.addChildElement("Password", "wsse");
		SOAPElement nonce = usernameToken.addChildElement("Nonce", "wsse");
		SOAPElement created = usernameToken.addChildElement("Created", "wsu");

		// Add the username to the Username SOAPElement
		usernameElement.addTextNode(username);

		// Generate and encode nonce, inserting it into the Nonce element
		Base64.Encoder e = Base64.getEncoder();
		byte[] nonceBinaryData = getNonce().getBytes("UTF-8");
		String nonceBase64 = e.encodeToString(nonceBinaryData);
		nonce.addTextNode(nonceBase64);

		// Get the date and save it into the Created element
		String utctimeStringData = getUTCTime();
		byte[] utctimeBinaryData = utctimeStringData.getBytes("UTF-8");
		created.addTextNode(utctimeStringData);

		// Encode the password and add it to the element
		byte[] passwordBinaryData = password.getBytes("UTF-8");
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		sha1.update(nonceBinaryData);
		sha1.update(utctimeBinaryData);
		sha1.update(passwordBinaryData);
		byte[] passwordDigest = sha1.digest();
		String passwordDigestBase64 = e.encodeToString(passwordDigest);
		passwordElement.addTextNode(passwordDigestBase64);
		System.out.println("Nonce: " + nonceBase64 + " | Date: " + utctimeStringData + " | Digest: " + passwordDigestBase64);
		QName typeQName = new QName("Type");
		passwordElement.addAttribute(typeQName, "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");

		msg.saveChanges();
		return msg;
	}

	protected String sendRequest(SOAPMessage msg) {
		String resp = "";
		try {
			URL url = new URL(endpoint);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");

			if (authenticate && !"".equals(username)) {
				addSecurityHeader(msg);
			} else {
				System.out.println("Sending unauthenticated request...");
			}

			String soapRequest = buildRequest(msg);

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = soapRequest.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					String inputLine;
					StringBuilder response = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}

					// Process the response here (XML parsing, etc.)
					resp = response.toString();
				}
			} else {
				resp = "Request failed. Response code: " + responseCode;
			}

			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
}
