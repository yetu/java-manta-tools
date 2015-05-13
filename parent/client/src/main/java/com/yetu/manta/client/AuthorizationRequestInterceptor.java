package com.yetu.manta.client;

import java.util.HashMap;
import java.util.Map;

import retrofit.RequestInterceptor;

import com.yetu.manta.httpsigner.HttpSigner;
import com.yetu.manta.httpsigner.HttpSignerException;

public class AuthorizationRequestInterceptor implements RequestInterceptor {

	private final static String AUTHZ_HEADER_KEY = "Authorization";

	private HttpSigner signer;
	
	private static Map<String, String> headersToSign = new HashMap<>();

	public AuthorizationRequestInterceptor(HttpSigner signer) {
		this.signer = signer;
	}

	public void intercept(RequestFacade request) {
		String authzValue;
		try {
			authzValue = signer.createAuthzHeaderValue(headersToSign);
			request.addHeader(AUTHZ_HEADER_KEY, authzValue);
		} catch (HttpSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
