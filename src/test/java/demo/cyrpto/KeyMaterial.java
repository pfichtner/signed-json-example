package demo.cyrpto;

import static lombok.AccessLevel.PRIVATE;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

import lombok.NoArgsConstructor;

/**
 * Generates a demo RSA key‑pair in memory so the sample can run without
 * external key infrastructure. NEVER do this in production.
 */
@NoArgsConstructor(access = PRIVATE)
public final class KeyMaterial {

	public static final RSAKey RSA_JWK = rsaKey("demo-key");

	private static RSAKey rsaKey(String keyId) {
		try {
			return new RSAKeyGenerator(2048).keyID(keyId).generate();
		} catch (Exception e) {
			throw new IllegalStateException("Error creating RSAKey", e);
		}
	}

}
