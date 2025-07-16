package demo.application;

import java.security.PublicKey;

/**
 * Strategy interface that allows the verifier to obtain a public key for a
 * given keyId. In real life this could hit a JWKS endpoint, db, or cache.
 */
public interface PublicKeyResolver {
	PublicKey resolve(String keyId);
}