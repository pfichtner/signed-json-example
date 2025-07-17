package demo.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.nimbusds.jose.jwk.RSAKey;

import demo.application.crypto.Base64String;
import demo.application.crypto.HashAlgorithm;
import demo.application.crypto.KeyId;
import demo.application.crypto.SignatureUtil;
import demo.application.crypto.SignatureVerificationException;
import demo.application.cyrpto.KeyGenerator;
import demo.application.cyrpto.TestPublicKeyResolver;
import demo.application.domain.Order;
import demo.application.domain.Order.Price;
import demo.application.domain.OrderApplicationService;
import demo.application.domain.OrderService;

class OrderApplicationServiceTest {

	private static final HashAlgorithm HASH_ALGORITHMN = new HashAlgorithm("SHA256withRSA");

	RSAKey testKeyPair = KeyGenerator.newRandomRsaKeyPair("some-random-key-id");

	UUID orderId = UUID.randomUUID();
	String name = "vacuum cleaner";
	double amount = 199.99;
	String currency = "EUR";

	Map<String, Object> payload = Map.of( //
			"id", orderId, //
			"name", name, //
			"price", Map.of("amount", amount, "currency", currency) //
	);

	private OrderService domainService = mock();

	private OrderApplicationService sut = new OrderApplicationService(new TestPublicKeyResolver(testKeyPair),
			domainService);

	@Test
	void verifiesValidSignature() throws Exception {
		sut.handle(orderId, payload, signature(), testKeyId(), HASH_ALGORITHMN);
		verify(domainService).create(orderId, new Order(orderId, name, new Price(amount, currency)));
		verifyNoMoreInteractions(domainService);
	}

	@Test
	void rejectsInvalidSignature() throws Exception {
		assertThatThrownBy(
				() -> sut.handle(orderId, addAttributeTo(payload), signature(), testKeyId(), HASH_ALGORITHMN))
				.isInstanceOf(SignatureVerificationException.class)
				.hasMessageContaining("Invalid", "signature", signature());
		verifyNoInteractions(domainService);
	}

	@Test
	void rejectsSignedByOtherKey() throws Exception {
		assertThatThrownBy(() -> sut.handle(orderId, addAttributeTo(payload), signature(),
				new KeyId(testKeyId().value() + "-other"), HASH_ALGORITHMN))
				.isInstanceOf(SignatureVerificationException.class);
		verifyNoInteractions(domainService);
	}

	private KeyId testKeyId() {
		return new KeyId(testKeyPair.getKeyID());
	}

	private Base64String signature() throws Exception {
		return SignatureUtil.createSignature(payload, testKeyPair.toPrivateKey(), HASH_ALGORITHMN);
	}

	private static Map<String, Object> addAttributeTo(Map<String, Object> payload) {
		var modifiedPayload = new HashMap<>(payload);
		modifiedPayload.put("orderWasChecked", true);
		return modifiedPayload;
	}

}
