package com.yetu.manta.httpsigner;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.util.encoders.Base64;

public class HttpSigner {

	private static final DateFormat HEADER_DATE_FORMAT = new SimpleDateFormat(
			"EEE MMM d HH:mm:ss yyyy zzz");
	private final static String DATE_HEADER_KEY = "date";
	private static final String AUTHZ_HEADER = "Signature keyId=\"/%s/keys/%s\",algorithm=\"rsa-sha256\", "
			+ "headers=\"%s\" signature=\"%s\"";

	static final String SIGNING_ALGORITHM = "SHA256WithRSAEncryption";

	private final JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
			.setProvider("BC");

	private final KeyPair keyPair;
	private final String login;

	private final String fingerPrint;

	public HttpSigner(String login, String keyPath, String keyFingerPrint)
			throws IOException {
		this.login = login;
		this.fingerPrint = keyFingerPrint;
		this.keyPair = getKeyPair(keyPath);
	}

	public HttpSigner(String login, String keyPath, String keyFingerPrint,
			String privateKeyContent, char[] password) throws IOException {
		this.login = login;
		this.fingerPrint = keyFingerPrint;
		this.keyPair = getKeyPair(privateKeyContent, password);
	}

	/**
	 * @param keyPath
	 * @return
	 * @throws IOException
	 *             If unable to read the private key from the file
	 */
	private KeyPair getKeyPair(final String keyPath) throws IOException {
		final BufferedReader br = new BufferedReader(new FileReader(keyPath));
		Security.addProvider(new BouncyCastleProvider());

		try (final PEMParser pemParser = new PEMParser(br)) {
			final Object object = pemParser.readObject();

			KeyPair kp = converter.getKeyPair((PEMKeyPair) object);

			return kp;
		}
	}

	/**
	 * Read KeyPair from a string, optionally using password.
	 *
	 * @param privateKeyContent
	 * @param password
	 * @return
	 * @throws IOException
	 *             If unable to read the private key from the string
	 */
	private KeyPair getKeyPair(final String privateKeyContent,
			final char[] password) throws IOException {
		byte[] pKeyBytes = privateKeyContent.getBytes();

		try (InputStream byteArrayStream = new ByteArrayInputStream(pKeyBytes);
				Reader inputStreamReader = new InputStreamReader(
						byteArrayStream);
				BufferedReader reader = new BufferedReader(inputStreamReader);
				PEMParser pemParser = new PEMParser(reader)) {

			PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
					.build(password);

			Object object = pemParser.readObject();

			final KeyPair kp;
			if (object instanceof PEMEncryptedKeyPair) {
				kp = converter.getKeyPair(((PEMEncryptedKeyPair) object)
						.decryptKeyPair(decProv));
			} else {
				kp = converter.getKeyPair((PEMKeyPair) object);
			}

			return kp;

		}
	}

	/**
	 * Sign an {@link HttpRequest}.
	 *
	 * @param request
	 *            The {@link HttpRequest} to sign.
	 * @throws MantaCryptoException
	 *             If unable to sign the request.
	 */
	public String createAuthzHeaderValue(final Map<String, String> headersToSign)
			throws HttpSignerException {
		if (this.keyPair == null) {
			throw new HttpSignerException("No KeyPair loaded");
		}
		String date = headersToSign.get(DATE_HEADER_KEY);
		if (date == null) {
			final Date now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
					.getTime();
			date = HEADER_DATE_FORMAT.format(now);
			headersToSign.put(DATE_HEADER_KEY, date);
		}
		try {
			final Signature sig = Signature.getInstance(SIGNING_ALGORITHM);
			sig.initSign(this.keyPair.getPrivate());
			final String signingString = createStringToSign(headersToSign);
			sig.update(signingString.getBytes("UTF-8"));
			final byte[] signedData = sig.sign();
			final byte[] encodedSignedData = Base64.encode(signedData);
			final String authzHeader = String.format(AUTHZ_HEADER, this.login,
					this.fingerPrint, createHeaderList(headersToSign),
					new String(encodedSignedData));
			return authzHeader;
		} catch (final NoSuchAlgorithmException e) {
			throw new HttpSignerException("Invalid Algorithm", e);
		} catch (final InvalidKeyException e) {
			throw new HttpSignerException("Invalid Key", e);
		} catch (final SignatureException e) {
			throw new HttpSignerException("Invalid Signature", e);
		} catch (final UnsupportedEncodingException e) {
			throw new HttpSignerException("Invalid Encoding", e);
		}
	}

	private String createStringToSign(final Map<String, String> headers) {
		StringBuilder builder = new StringBuilder();
		int headerCount = headers.size();
		int counter = 0;
		for (Entry<String, String> headerPair : headers.entrySet()) {
			String headerKey = headerPair.getKey();
			String headerValue = headerPair.getValue();
			builder.append(headerKey);
			builder.append(": ");
			builder.append(headerValue);
			if ((counter + 1) < headerCount && headerCount > 1) {
				builder.append("\n");
			}

			counter++;
		}

		return builder.toString();
	}

	private String createHeaderList(final Map<String, String> headers) {
		StringBuilder builder = new StringBuilder();
		int headerCount = headers.size();
		int counter = 0;
		for (String headerKey : headers.keySet()) {
			builder.append(headerKey);
			if ((counter - 1) < headerCount && headerCount > 1) {
				builder.append(" ");
			}
			counter++;

		}

		return builder.toString();
	}

}
