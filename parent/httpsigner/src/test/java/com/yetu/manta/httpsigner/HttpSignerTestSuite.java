package com.yetu.manta.httpsigner;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HttpSignerTestSuite {

	private final static String AUTHZ_PATTERN = "^Signature keyId=\"(.+)\",algorithm=\"rsa-sha256\", headers=\"(.+)\" signature=\"(.+)\"$";

	private final static String testKeyFingerprint = "8f:75:0f:cb:02:20:6d:d5:97:28:f1:89:c4:72:d4:02";

	private HttpSigner signer;

	private final static Pattern EXPECTED_AUTHZ_PATTERN = Pattern
			.compile(AUTHZ_PATTERN);

	@Before
	public void setupSigner() throws Exception {
		signer = new HttpSigner("testUser", "./src/test/resources/manta",
				testKeyFingerprint);
	}

	@Test
	public void testSigningOfEmptyHeaderMap() throws Exception {

		Map<String, String> headers = new HashMap<>();
		String authzHeader = signer.createAuthzHeaderValue(headers);

		assertHeaderValueMatchesPattern(authzHeader,"date");
	}

	@Test
	public void testSigningWithDateHeader() throws Exception {
		Map<String, String> headers = new HashMap<>();
		headers.put("date", "Wed, 13 May 2015 12:45:26 GMT");
		String authZHeader = signer.createAuthzHeaderValue(headers);
		assertHeaderValueMatchesPattern(authZHeader,"date");
	}

	private void assertHeaderValueMatchesPattern(String authzHeader, String expectedHeaderList) {
		Matcher m = EXPECTED_AUTHZ_PATTERN.matcher(authzHeader);
		Assert.assertTrue(m.matches());
		String expectedKeyId = "/testUser/keys/" + testKeyFingerprint;
		String extractedKeyId = m.group(1);
		Assert.assertEquals(expectedKeyId, extractedKeyId);

		Assert.assertEquals(expectedHeaderList, m.group(2));

		Assert.assertNotNull(m.group(3));
	}
}
