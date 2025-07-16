package demo.application.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.application.domain.OrderApplicationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderApplicationService orderService;

	@PutMapping("/{id}")
	public void receive(@PathVariable("id") UUID id, @RequestBody SignedPayloadDTO dto) {
		orderService.handle(id, dto.payload(), dto.signature(), dto.keyId(), dto.algorithm());
	}
}
