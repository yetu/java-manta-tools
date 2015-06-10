package com.yetu.manta.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.mime.TypedFile;

import com.yetu.manta.client.converter.LineDelimitedJsonConverter;
import com.yetu.manta.client.representations.MantaObject;
import com.yetu.manta.httpsigner.HttpSigner;

public class MantaClient {

	public final static String DEFAULT_API_URL = "https://us-east.manta.joyent.com";

	private MantaClientInterface mantaClient;

	private HttpSigner signer;

	private final String login;

	private String apiUrl;

	// Read only access
	/**
	 * Create a read only MantaClient
	 * @param login
	 * @throws IOException
	 */
	public MantaClient(String login) throws IOException {
		this(login, null, null);
	}

	// R/W access
	/**
	 * Create a MantaClient with the possibility of R/W access
	 * @param login
	 * @param keyPath Path to the private key
	 * @param keyFingerprint Fingerprint of the public key
	 * @throws IOException
	 */
	public MantaClient(String login, String keyPath, String keyFingerprint)
			throws IOException {
		this.apiUrl = DEFAULT_API_URL;
		this.login = login;
		if (keyPath != null && keyFingerprint != null) {
			signer = new HttpSigner(login, keyPath, keyFingerprint);
		}
		RestAdapter.Builder builder = new RestAdapter.Builder();
		builder.setEndpoint(apiUrl).setConverter(
				new LineDelimitedJsonConverter());
		if (this.signer != null) {
			builder.setRequestInterceptor(new AuthorizationRequestInterceptor(
					signer));
		}
		RestAdapter restAdapter = builder.build();

		mantaClient = restAdapter.create(MantaClientInterface.class);
	}

	/**
	 * Return login name
	 * @return
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * Returns true if only read access of public areas is possible
	 * @return
	 */
	public boolean isReadOnly() {
		return signer == null;
	}

	/**
	 * List all MantaObjects in given path (only works with directories)
	 * @param path
	 * @return
	 */
	public Collection<MantaObject> listMantaObjects(String path) {
		return mantaClient.list(login, path);
	}

	/**
	 * Get InputStream of non-directory MantaObject at given Path
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public InputStream getObjectInputStream(String path) throws IOException {
		return mantaClient.getRawObject(login, path).getBody().in();
	}

	/**
	 * Upload the specified file to the specified path
	 * @param path
	 * @param file
	 * @throws FileNotFoundException
	 */
	public void putFile(String path, File file) throws FileNotFoundException {
		TypedFile tf = new TypedFile("application/binary", file);
		mantaClient.putFile(login, path, tf);
	}

	/**
	 * Delete the MantaObject at the given path
	 * @param path
	 */
	public void deleteFile(String path) {
		mantaClient.deleteObject(login, path);
	}

}
