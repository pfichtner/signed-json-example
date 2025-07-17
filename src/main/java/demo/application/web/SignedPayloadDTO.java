package demo.application.web;

import java.util.Map;

import demo.application.crypto.Base64String;
import demo.application.crypto.KeyId;

public record SignedPayloadDTO(Map<String, Object> payload, Base64String signature, KeyId keyId, String algorithm) {
}
