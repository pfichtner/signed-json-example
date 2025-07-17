package demo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;

import demo.application.crypto.Base64String;
import demo.application.crypto.SignatureVerificationException;
import demo.application.crypto.SignatureVerifier;
import demo.application.cyrpto.KeyGenerator;
import demo.application.cyrpto.PayloadSigner;
import demo.application.domain.Order;
import demo.application.domain.Order.Price;

class SignatureVerifierTest {

	private static final String HASH_ALGORITHMN = "SHA256withRSA";

	RSAKey testKeyPair = KeyGenerator.newRandomRsaKeyPair("demo-key");

	UUID orderId = UUID.randomUUID();
	double amount = 199.99;
	String currency = "EUR";

	Map<String, Object> payload = Map.of("id", orderId, "price", Map.of("amount", amount, "currency", currency));

	private SignatureVerifier verifier = new SignatureVerifier(new TestPublicKeyResolver(testKeyPair));

	@Test
	void verifiesValidSignature() throws Exception {
		var deserialized = verifier.verifyAndMap(payload, createSignature(), testKeyPair.getKeyID(), HASH_ALGORITHMN,
				Order.class);
		var expected = new Order(orderId, new Price(amount, currency));
		assertThat(deserialized).isEqualTo(expected);
	}

	@Test
	void rejectsInvalidSignature() throws Exception {
		assertThatThrownBy(() -> verifier.verifyAndMap(addAttributeTo(payload), createSignature(),
				testKeyPair.getKeyID(), HASH_ALGORITHMN, Order.class))
				.isInstanceOf(SignatureVerificationException.class);
	}

	private Base64String createSignature() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
			JsonProcessingException, JOSEException {
		return new PayloadSigner(testKeyPair.toPrivateKey(), HASH_ALGORITHMN).sign(payload);
	}

	private static Map<String, Object> addAttributeTo(Map<String, Object> payload) {
		var modifiedPayload = new HashMap<>(payload);
		modifiedPayload.put("orderWasChecked", true);
		return modifiedPayload;
	}

}
