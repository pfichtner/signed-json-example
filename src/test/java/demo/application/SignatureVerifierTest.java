package demo.application;

import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;

import demo.cyrpto.KeyMaterial;
import demo.domain.Order;
import demo.domain.Order.Price;

class SignatureVerifierTest {

	RSAKey testKeyPair = KeyMaterial.RSA_JWK;

	UUID orderId = UUID.randomUUID();
	double amount = 199.99;
	String currency = "EUR";

	Map<String, Object> payload = Map.of("id", orderId, "price", Map.of("amount", amount, "currency", currency));

	private SignatureVerifier verifier = new SignatureVerifier(kid -> {
		if (!kid.equals(testKeyPair.getKeyID())) {
			throw new IllegalArgumentException("Unknown keyId " + kid);
		}
		try {
			return testKeyPair.toRSAPublicKey();
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}
	});

	@Test
	void verifiesValidSignature() throws Exception {
		String json = new ObjectMapper().configure(ORDER_MAP_ENTRIES_BY_KEYS, true).writeValueAsString(payload);
		String signature = sign(json, testKeyPair.toPrivateKey());
		var deserialized = verifier.verifyAndMap(payload, signature, testKeyPair.getKeyID(), Order.class);
		var expected = new Order(orderId, new Price(amount, currency));
		assertThat(deserialized).isEqualTo(expected);
	}

	@Test
	void rejectsInvalidSignature() throws Exception {
		String json = new ObjectMapper().configure(ORDER_MAP_ENTRIES_BY_KEYS, true).writeValueAsString(payload);
		String signature = sign(json, testKeyPair.toPrivateKey());
		assertThatThrownBy(
				() -> verifier.verifyAndMap(addAttributeTo(payload), signature, testKeyPair.getKeyID(), Order.class))
				.isInstanceOf(SecurityException.class);
	}

	private static HashMap<String, Object> addAttributeTo(Map<String, Object> payload) {
		var modifiedPayload = new HashMap<>(payload);
		modifiedPayload.put("orderWasChecked", true);
		return modifiedPayload;
	}

	private String sign(String documentToSign, PrivateKey privateKey)
			throws NoSuchAlgorithmException, InvalidKeyException, JOSEException, SignatureException {
		Signature signer = Signature.getInstance("SHA256withRSA");
		signer.initSign(privateKey);
		signer.update(documentToSign.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(signer.sign());
	}

}
