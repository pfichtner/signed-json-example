package demo.application;

import java.util.Map;
import java.util.UUID;

import demo.domain.Order;
import demo.domain.OrderService;
import lombok.RequiredArgsConstructor;

/** Example use‑case orchestration: verify signature, then call domain. */
@RequiredArgsConstructor
public class OrderApplicationService {

	private final SignatureVerifier verifier;
	private final OrderService domainService;

	public void handle(UUID id, Map<String, Object> payload, String signature, String keyId) {
		Order order = verifier.verifyAndMap(payload, signature, keyId, Order.class);
		domainService.create(id, order);
	}
}
