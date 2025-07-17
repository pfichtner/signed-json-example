package demo.application.cyrpto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import demo.application.crypto.JsonNormalizer;

public class PayloadSigner {

	private final Signature signer;
	private final Encoder encoder = Base64.getEncoder();
	private final JsonNormalizer jsonNormalizer = new JsonNormalizer();

	public PayloadSigner(PrivateKey privateKey, String hashAlgorithm) {
		try {
			signer = Signature.getInstance(hashAlgorithm);
			signer.initSign(privateKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public String sign(Map<String, Object> payload)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, JsonProcessingException {
		String normalizedDocumentToSign = jsonNormalizer.normalize(payload);
		byte[] signature;
		synchronized (signer) {
			signer.update(normalizedDocumentToSign.getBytes(StandardCharsets.UTF_8));
			signature = signer.sign();
		}
		return encoder.encodeToString(signature);
	}

}
