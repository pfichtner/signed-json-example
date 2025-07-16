package demo.application;

import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/** Centralized application‑layer verification component */
@RequiredArgsConstructor
public class SignatureVerifier {

	private final PublicKeyResolver keyResolver;
	private final ObjectMapper canonicalMapper;

	public SignatureVerifier(PublicKeyResolver keyResolver) {
		this(keyResolver, new ObjectMapper().configure(ORDER_MAP_ENTRIES_BY_KEYS, true));
	}

	/** Verifies signature and converts the payload to the requested target type. */
	public <T> T verifyAndMap(Map<String, Object> payload, String signatureBase64, String keyId, Class<T> targetType) {
		verify(payload, signatureBase64, keyId);
		return canonicalMapper.convertValue(payload, targetType);
	}

	/** Verifies signature . */
	public <T> void verify(Map<String, Object> payload, String signatureBase64, String keyId) {
		try {
			String normalizedJsonString = normalizedJsonString(payload);
			byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

			if (!verifySignature(keyId, normalizedJsonString, signatureBytes)) {
				throw new SecurityException("Invalid signature");
			}
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Cannot serialize payload", e);
		} catch (Exception e) {
			throw new SecurityException("Signature verification failed", e);
		}
	}

	private String normalizedJsonString(Map<String, Object> payload) throws JsonProcessingException {
		return canonicalMapper.writeValueAsString(payload);
	}

	private boolean verifySignature(String keyId, String signedDocument, byte[] signatureBytes)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		PublicKey publicKey = keyResolver.resolve(keyId);
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(publicKey);
		sig.update(signedDocument.getBytes(StandardCharsets.UTF_8));
		return sig.verify(signatureBytes);
	}

}
