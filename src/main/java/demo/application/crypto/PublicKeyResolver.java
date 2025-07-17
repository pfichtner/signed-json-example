package demo.application.crypto;

import java.security.PublicKey;

public interface PublicKeyResolver {
	PublicKey resolve(KeyId keyId);
}