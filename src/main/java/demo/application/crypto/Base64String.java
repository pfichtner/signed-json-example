package demo.application.crypto;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public record Base64String(String value) {

	private static final Encoder encoder = Base64.getEncoder();
	private static final Decoder decoder = Base64.getDecoder();

	public Base64String(byte[] bytes) {
		this(encoder.encodeToString(bytes));
	}

	byte[] decode() {
		return decoder.decode(value());
	}

}
