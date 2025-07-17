package demo.application.crypto;

import com.fasterxml.jackson.annotation.JsonValue;

public record KeyId(@JsonValue String value) {

	public boolean valueIs(String otherId) {
		return value().equals(otherId);
	}

}
