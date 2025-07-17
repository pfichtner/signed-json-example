package demo.application.domain;

import static demo.application.crypto.SignatureUtil.verifySignature;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import demo.application.crypto.Base64String;
import demo.application.crypto.HashAlgorithm;
import demo.application.crypto.KeyId;
import demo.application.crypto.PublicKeyResolver;
import demo.application.crypto.SignatureVerificationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final PublicKeyResolver keyResolver;
	private final OrderService domainService;

	public void handle(UUID id, Map<String, Object> payload, Base64String signature, KeyId keyId,
			HashAlgorithm hashAlgorithmn) {
		if (!isSignatureOk(payload, signature, keyId, hashAlgorithmn)) {
			throw new SignatureVerificationException("Invalid signature " + signature.value());
		}
		domainService.create(id, objectMapper.convertValue(payload, Order.class));
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
