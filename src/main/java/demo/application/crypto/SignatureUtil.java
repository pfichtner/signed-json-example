package demo.application.crypto;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Map;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SignatureUtil {

	private static final JsonNormalizer jsonNormalizer = new JsonNormalizer();

	public static Base64String createSignature(Map<String, Object> payload, PrivateKey privateKey,
			HashAlgorithm hashAlgorithmn) throws Exception {
		var normalizedDocumentToSign = jsonNormalizer.normalize(payload);
		var signer = Signature.getInstance(hashAlgorithmn.value());
		signer.initSign(privateKey);
		signer.update(normalizedDocumentToSign.getBytes(UTF_8));
		return new Base64String(signer.sign());
	}

	public static boolean verifySignature(Map<String, Object> payload, PublicKey publicKey,
			HashAlgorithm hashAlgorithmn, Base64String signatureToVerify) throws SignatureException, Exception {
		var signature = Signature.getInstance(hashAlgorithmn.value());
		signature.initVerify(publicKey);
		signature.update(jsonNormalizer.normalize(payload).getBytes(UTF_8));
		return signature.verify(signatureToVerify.decode());
	}

}
