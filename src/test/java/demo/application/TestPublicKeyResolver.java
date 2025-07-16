package demo.application;

import java.security.PublicKey;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;

import demo.application.crypto.PublicKeyResolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestPublicKeyResolver implements PublicKeyResolver {

	private final RSAKey testKeyPair;

	@Override
	public PublicKey resolve(String kid) {
		if (!kid.equals(testKeyPair.getKeyID())) {
			throw new IllegalArgumentException("Unknown keyId " + kid);
		}
		try {
			return testKeyPair.toRSAPublicKey();
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}
	}

}