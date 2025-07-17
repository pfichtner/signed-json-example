package demo.application.cyrpto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import demo.application.crypto.Base64String;
import demo.application.crypto.HashAlgorithm;
import demo.application.crypto.JsonNormalizer;

public class PayloadSigner {

	private final Signature signer;
	private final JsonNormalizer jsonNormalizer = new JsonNormalizer();

	public PayloadSigner(PrivateKey privateKey, HashAlgorithm hashAlgorithm) {
		try {
			signer = Signature.getInstance(hashAlgorithm.value());
			signer.initSign(privateKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public Base64String sign(Map<String, Object> payload)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, JsonProcessingException {
		String normalizedDocumentToSign = jsonNormalizer.normalize(payload);
		byte[] signature;
		synchronized (signer) {
			signer.update(normalizedDocumentToSign.getBytes(StandardCharsets.UTF_8));
			signature = signer.sign();
		}
		return new Base64String(signature);
	}

}
