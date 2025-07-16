package demo.application.domain;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import demo.application.crypto.SignatureVerifier;
import lombok.RequiredArgsConstructor;

/** Example use‑case orchestration: verify signature, then call domain. */
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

	private final SignatureVerifier verifier;
	private final OrderService domainService;

	public void handle(UUID id, Map<String, Object> payload, String signature, String keyId, String hashAlgorithmn) {
		Order order = verifier.verifyAndMap(payload, signature, keyId, hashAlgorithmn, Order.class);
		domainService.create(id, order);
	}
}
