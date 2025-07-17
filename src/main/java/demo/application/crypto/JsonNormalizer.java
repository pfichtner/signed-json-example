package demo.application.crypto;

import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonNormalizer {

	private final ObjectMapper canonicalMapper = new ObjectMapper().configure(ORDER_MAP_ENTRIES_BY_KEYS, true);
	private static final TypeReference<Map<String, Object>> VALUE_TYPE_REF = new TypeReference<Map<String, Object>>() {
	};

	public <T> T normalize(Map<String, Object> payload, Class<T> targetType) {
		return canonicalMapper.convertValue(payload, targetType);
	}

	public String normalize(String payload) {
		try {
			return normalize(canonicalMapper.readValue(payload, VALUE_TYPE_REF));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public String normalize(Map<String, Object> payload) {
		try {
			return canonicalMapper.writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}