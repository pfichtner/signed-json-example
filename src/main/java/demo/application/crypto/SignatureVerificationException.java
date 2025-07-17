package demo.application.crypto;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ResponseStatus(FORBIDDEN)
public class SignatureVerificationException extends RuntimeException {

	private static final long serialVersionUID = -4681170620321383594L;

	public SignatureVerificationException(String message) {
		super(message);
	}

	public SignatureVerificationException(String message, Exception cause) {
		super(message, cause);
	}

}
