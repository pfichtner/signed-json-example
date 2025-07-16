package demo.application.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;

import demo.application.TestPublicKeyResolver;
import demo.application.crypto.PublicKeyResolver;
import demo.application.cyrpto.KeyGenerator;
import demo.application.cyrpto.PayloadSigner;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerMockMvcTest {

	@TestConfiguration
	static class Config {

		static String kid = "demo-key";
		static RSAKey keyPair = KeyGenerator.newRandomRsaKeyPair(kid);

		@Bean
		PublicKeyResolver publicKeyResolver() {
			return new TestPublicKeyResolver(keyPair);
		}

	}

	@Autowired
	MockMvc mockMvc;

	@Test
	void accepts() throws Exception {
		var id = "3aebf66c-d8e5-456a-887c-53e5fc45f0a1";
		var payload = """
				{
					"id": "%s",
					"price": {
						"amount": 1.99,
						"currency": "EUR"
					}
				}
				""".formatted(id);
		var payloadWithSignature = payloadWithSignature(payload);
		mockMvc.perform(put("/orders/%s".formatted(id)) //
				.contentType(APPLICATION_JSON) //
				.content(payloadWithSignature)) //
				.andExpect(status().isOk());
	}

	@Test
	void refuses() throws Exception {
		var id = "3aebf66c-d8e5-456a-887c-53e5fc45f0a1";
		var payload = """
				{
					"id": "%s",
					"price": {
						"amount": 1.99,
						"currency": "EUR"
					}
				}
				""".formatted(id);
		var payloadWithSignature = payloadWithSignature(payload);
		var manipulatedJson = manipulateJson(payloadWithSignature);

		mockMvc.perform(put("/orders/%s".formatted(id)) //
				.contentType(APPLICATION_JSON) //
				.content(manipulatedJson)) //
				.andExpect(status().isForbidden());
	}

	@SuppressWarnings("unchecked")
	private String manipulateJson(String payloadWithSignature) throws JsonProcessingException, JsonMappingException {
		var json = jsonToMap(payloadWithSignature);
		var payload = (Map<String, Object>) json.get("payload");
		var price = (Map<String, Object>) payload.get("price");
		price.put("currency", price.get("currency") + "XXX");
		return mapToJson(json);
	}

	private String payloadWithSignature(String rawPayload) throws JsonProcessingException, JsonMappingException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException, JOSEException {
		var data = jsonToMap(rawPayload);
		var signature = new PayloadSigner(Config.keyPair.toPrivateKey(), "SHA256withRSA").sign(data);
		var payload = Map.of("payload", data, "signature", signature, "keyId", Config.kid, "algorithm",
				"SHA256withRSA");
		return mapToJson(payload);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> jsonToMap(String rawPayload) throws JsonProcessingException, JsonMappingException {
		return new ObjectMapper().readValue(rawPayload, Map.class);
	}

	private String mapToJson(Map<String, Object> payload) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(payload);
	}

}
