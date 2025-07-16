package demo.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.application.OrderApplicationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class SignedPayloadController {

	private final OrderApplicationService orderService;

	@PutMapping("/{id}")
	public ResponseEntity<Void> receive(@PathVariable("id") UUID id, @RequestBody SignedPayloadDTO dto) {
		orderService.handle(id,dto.payload(), dto.signature(), dto.keyId());
		return ResponseEntity.ok().build();
	}
}
