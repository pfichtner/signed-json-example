package demo.application.web;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;

import demo.application.TestPublicKeyResolver;
import demo.application.crypto.HashAlgorithm;
import demo.application.crypto.PublicKeyResolver;
import demo.application.cyrpto.KeyGenerator;
import demo.application.cyrpto.PayloadSigner;
import demo.application.domain.Order;
import demo.application.domain.Order.Price;
import demo.application.domain.OrderService;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerMockMvcTest {

	@TestConfiguration
	static class Config {

		@Bean
		RSAKey keyPair() {
			return KeyGenerator.newRandomRsaKeyPair("demo-key");
		}

		@Bean
		PublicKeyResolver publicKeyResolver(RSAKey keyPair) {
			return new TestPublicKeyResolver(keyPair);
		}

	}

	@Autowired
	MockMvc mockMvc;

	@Autowired
	RSAKey keyPair;

	@MockitoBean
	OrderService orderService;

	@Test
	void accepts() throws Exception {
		var id = UUID.fromString("3aebf66c-d8e5-456a-887c-53e5fc45f0a1");
		var order = """
				{
					"id": "%s",
					"price": {
						"amount": 1.99,
						"currency": "EUR"
					}
				}
				""".formatted(id);
		var payloadWithSignature = payloadWithSignature(order, keyPair.toPrivateKey());
		mockMvc.perform(put("/orders/%s".formatted(id)) //
				.contentType(APPLICATION_JSON) //
				.content(payloadWithSignature)) //
				.andExpect(status().isOk());
		verify(orderService).create(id, new Order(id, new Price(1.99, "EUR")));
		verifyNoMoreInteractions(orderService);
	}

	@Test
	void acceptsReformatted() throws Exception {
		var id = UUID.fromString("3aebf66c-d8e5-456a-887c-53e5fc45f0a1");
		var order = """
				{
					"id": "%s",
					"price": {
						"amount": 1.99,
						"currency": "EUR"
					}
				}
				""".formatted(id);
		var payloadWithSignature = payloadWithSignature(order, keyPair.toPrivateKey());
		var reformattedJson = payloadWithSignature.lines().map(l -> format("   \t  %s \t  ", l)).collect(joining("\n"));
		assert !payloadWithSignature.equals(reformattedJson);
		mockMvc.perform(put("/orders/%s".formatted(id)) //
				.contentType(APPLICATION_JSON) //
				.content(reformattedJson)) //
				.andExpect(status().isOk());
		verify(orderService).create(id, new Order(id, new Price(1.99, "EUR")));
		verifyNoMoreInteractions(orderService);
	}

	@Test
	void refusesBecauseModified() throws Exception {
		var id = "3aebf66c-d8e5-456a-887c-53e5fc45f0a1";
		var order = """
				{
					"id": "%s",
					"price": {
						"amount": 1.99,
						"currency": "EUR"
					}
				}
				""".formatted(id);
		var payloadWithSignature = payloadWithSignature(order, keyPair.toPrivateKey());
		var manipulatedJson = manipulateJson(payloadWithSignature);

		mockMvc.perform(put("/orders/%s".formatted(id)) //
				.contentType(APPLICATION_JSON) //
				.content(manipulatedJson)) //
				.andExpect(status().isForbidden());
		verifyNoInteractions(orderService);
	}

	@Test
	void refusesBecauseSignedByUnknownKey() throws Exception {
		var id = "3aebf66c-d8e5-456a-887c-53e5fc45f0a1";
		var order = """
				{
					"id": "%s",
					"price": {
						"amount": 1.99,
						"currency": "EUR"
					}
				}
				""".formatted(id);
		PrivateKey otherPrivateKey = KeyGenerator.newRandomRsaKeyPair(keyPair.getKeyID() + "-another").toPrivateKey();
		var payloadWithSignature = payloadWithSignature(order, otherPrivateKey);
		var manipulatedJson = manipulateJson(payloadWithSignature);

		mockMvc.perform(put("/orders/%s".formatted(id)) //
				.contentType(APPLICATION_JSON) //
				.content(manipulatedJson)) //
				.andExpect(status().isForbidden());
		verifyNoInteractions(orderService);
	}

	@SuppressWarnings("unchecked")
	private String manipulateJson(String payloadWithSignature) throws JsonProcessingException, JsonMappingException {
		var json = jsonToMap(payloadWithSignature);
		var payload = (Map<String, Object>) json.get("payload");
		var price = (Map<String, Object>) payload.get("price");
		price.put("currency", price.get("currency") + "XXX");
		return mapToJson(json);
	}

	private String payloadWithSignature(String rawPayload, PrivateKey privateKey) throws JsonProcessingException,
			JsonMappingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, JOSEException {
		var data = jsonToMap(rawPayload);
		var hashAlgorithm = new HashAlgorithm("SHA256withRSA");
		var signature = new PayloadSigner(privateKey, hashAlgorithm).sign(data);
		return mapToJson(Map.of("payload", data, "signature", signature, "keyId", keyPair.getKeyID(), "algorithm",
				hashAlgorithm));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> jsonToMap(String rawPayload) throws JsonProcessingException, JsonMappingException {
		return new ObjectMapper().readValue(rawPayload, Map.class);
	}

	private String mapToJson(Map<String, Object> payload) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(payload);
	}

}
