package demo.application.cyrpto;

import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Base64.Encoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class PayloadSigner {

	private final Signature signer;
	private final Encoder encoder;
	private final ObjectMapper objectMapper;

	public PayloadSigner(PrivateKey privateKey, String hashAlgorithm) {
		objectMapper = new ObjectMapper().configure(ORDER_MAP_ENTRIES_BY_KEYS, true);
		try {
			signer = Signature.getInstance(hashAlgorithm);
			signer.initSign(privateKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		encoder = Base64.getEncoder();
	}

	public String sign(Map<?, ?> map)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, JsonProcessingException {
		String normalizedDocumentToSign = objectMapper.writeValueAsString(map);
		byte[] signature;
		synchronized (signer) {
			signer.update(normalizedDocumentToSign.getBytes(StandardCharsets.UTF_8));
			signature = signer.sign();
		}
		return encoder.encodeToString(signature);
	}

}
