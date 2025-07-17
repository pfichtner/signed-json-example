package demo.application.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SignatureVerifier {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final JsonNormalizer jsonNormalizer = new JsonNormalizer();
	private final PublicKeyResolver keyResolver;

	/**
	 * Verifies signature and converts the payload to the requested target type.
	 * 
	 * @param hashAlgorithmn TODO
	 */
	public <T> T verifyAndMap(Map<String, Object> payload, Base64String signature, String keyId, String hashAlgorithmn,
			Class<T> targetType) {
		verify(payload, signature, keyId, hashAlgorithmn);
		return objectMapper.convertValue(payload, targetType);
	}

	public <T> void verify(Map<String, Object> payload, Base64String signature, String keyId, String hashAlgorithmn) {
		if (!isSignatureOk(payload, signature, keyId, hashAlgorithmn)) {
			throw new SignatureVerificationException("Invalid signature");
		}
	}

	private boolean isSignatureOk(Map<String, Object> payload, Base64String signature, String keyId,
			String hashAlgorithmn) {
		try {
			return signature(keyResolver.resolve(keyId), jsonNormalizer.normalize(payload), hashAlgorithmn)
					.verify(signature.decode());
		} catch (Exception e) {
			throw new SignatureVerificationException("Signature verification failed", e);
		}
	}

	private Signature signature(PublicKey publicKey, String signedDocument, String hashAlgorithm)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		var signature = Signature.getInstance(hashAlgorithm);
		signature.initVerify(publicKey);
		signature.update(signedDocument.getBytes(StandardCharsets.UTF_8));
		return signature;
	}

}
