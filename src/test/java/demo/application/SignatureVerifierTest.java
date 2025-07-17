package demo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import demo.application.crypto.SignatureVerifier;
import demo.application.cyrpto.KeyGenerator;
import demo.application.domain.Order;
import demo.application.domain.Order.Price;

class SignatureVerifierTest {

	private static final HashAlgorithm HASH_ALGORITHMN = new HashAlgorithm("SHA256withRSA");

	RSAKey testKeyPair = KeyGenerator.newRandomRsaKeyPair("some-random-key-id");

	UUID orderId = UUID.randomUUID();
	String name = "toothbrush";
	double amount = 199.99;
	String currency = "EUR";

	Map<String, Object> payload = Map.of("id", orderId, //
			"name", "toothbrush", //
			"price", Map.of("amount", amount, "currency", currency));

	private SignatureVerifier verifier = new SignatureVerifier(new TestPublicKeyResolver(testKeyPair));

	@Test
	void verifiesValidSignature() throws Exception {
		var deserialized = verifier.verifyAndMap(payload, signature(), testKeyId(), HASH_ALGORITHMN, Order.class);
		var expected = new Order(orderId, name, new Price(amount, currency));
		assertThat(deserialized).isEqualTo(expected);
	}

	@Test
	void rejectsInvalidSignature() throws Exception {
		assertThatThrownBy(() -> verifier.verifyAndMap(addAttributeTo(payload), signature(), testKeyId(),
				HASH_ALGORITHMN, Order.class)).isInstanceOf(SignatureVerificationException.class)
				.hasMessageContaining("Invalid", "signature", signature());
	}

	@Test
	void rejectsSignedByOtherKey() throws Exception {
		assertThatThrownBy(() -> verifier.verifyAndMap(payload, signature(), new KeyId(testKeyId().value() + "-other"),
				HASH_ALGORITHMN, Order.class)).isInstanceOf(SignatureVerificationException.class);
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
