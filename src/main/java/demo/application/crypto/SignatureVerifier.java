package demo.application.crypto;

import static demo.application.crypto.SignatureUtil.verifySignature;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SignatureVerifier {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final PublicKeyResolver keyResolver;

	public <T> T verifyAndMap(Map<String, Object> payload, Base64String signature, KeyId keyId,
			HashAlgorithm hashAlgorithmn, Class<T> targetType) {
		if (!isSignatureOk(payload, signature, keyId, hashAlgorithmn)) {
			throw new SignatureVerificationException("Invalid signature " + signature.value());
		}
		return objectMapper.convertValue(payload, targetType);
	}

	private boolean isSignatureOk(Map<String, Object> payload, Base64String signature, KeyId keyId,
			HashAlgorithm hashAlgorithmn) {
		try {
			return verifySignature(payload, keyResolver.resolve(keyId), hashAlgorithmn, signature);
		} catch (Exception e) {
			throw new SignatureVerificationException("Signature verification failed", e);
		}
	}

}
