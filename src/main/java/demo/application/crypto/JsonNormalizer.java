package demo.application.crypto;

import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonNormalizer {

	private final ObjectMapper canonicalMapper = new ObjectMapper().configure(ORDER_MAP_ENTRIES_BY_KEYS, true);

	public String normalize(Map<String, Object> payload) {
		try {
			return canonicalMapper.writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}