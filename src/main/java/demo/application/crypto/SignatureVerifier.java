package demo.application.crypto;

import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SignatureVerifier {

	private final ObjectMapper canonicalMapper = new ObjectMapper().configure(ORDER_MAP_ENTRIES_BY_KEYS, true);
	private final JsonNormalizer jsonNormalizer = new JsonNormalizer();
	private final PublicKeyResolver keyResolver;

	/**
	 * Verifies signature and converts the payload to the requested target type.
	 * 
	 * @param hashAlgorithmn TODO
	 */
	public <T> T verifyAndMap(Map<String, Object> payload, String signatureBase64, String keyId, String hashAlgorithmn,
			Class<T> targetType) {
		verify(payload, signatureBase64, keyId, hashAlgorithmn);
		return canonicalMapper.convertValue(payload, targetType);
	}

	public <T> void verify(Map<String, Object> payload, String signatureBase64, String keyId, String hashAlgorithmn) {
		if (!isSignatureOk(payload, signatureBase64, keyId, hashAlgorithmn)) {
			throw new SignatureVerificationException("Invalid signature");
		}
	}

	private boolean isSignatureOk(Map<String, Object> payload, String signatureBase64, String keyId,
			String hashAlgorithmn) {
		try {
			byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

			String normalizedJsonString = jsonNormalizer.normalize(payload);
			var signature = signature(keyResolver.resolve(keyId), normalizedJsonString, hashAlgorithmn);
			return signature.verify(signatureBytes);
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
