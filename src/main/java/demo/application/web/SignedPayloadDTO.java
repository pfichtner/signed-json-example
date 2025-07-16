package demo.application.web;

import java.util.Map;

public record SignedPayloadDTO(
        Map<String, Object> payload,
        String signature,
        String keyId,
        String algorithm) {
}
