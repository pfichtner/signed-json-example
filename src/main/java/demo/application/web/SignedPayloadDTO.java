package demo.application.web;

import java.util.Map;

import demo.application.crypto.Base64String;

public record SignedPayloadDTO(
        Map<String, Object> payload,
        Base64String signature,
        String keyId,
        String algorithm) {
}
