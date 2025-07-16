package demo.application.web;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import demo.application.crypto.SignatureVerifier;
import demo.application.domain.Order;
import demo.application.domain.Order.Price;
import demo.application.domain.OrderApplicationService;
import demo.application.domain.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

	@Mock
	SignatureVerifier verifier = mock();
	@Mock
	OrderService domainService = mock();
	@InjectMocks
	OrderApplicationService service;

	@Test
	void delegatesToVerifierAndDomainService() {
		OrderController sut = new OrderController(service);
		UUID anyUUID = UUID.fromString("22e1fa97-e3bb-4b1a-8d94-e81d54089fb4");

		Price price = new Price(1.99, "ABC");
		Order order = new Order(anyUUID, price);
		Map<String, Object> payload = Map.of("orderId", order.getId(), "price",
				Map.of("amount", price.getAmount(), "currency", price.getCurrency()));
		when(verifier.verifyAndMap(payload, "someSignature", "someKid", "someHashAlgorithm", Order.class))
				.thenReturn(order);

		sut.receive(anyUUID, new SignedPayloadDTO(payload, "someSignature", "someKid", "someHashAlgorithm"));

		verify(domainService).create(anyUUID, order);
		verifyNoMoreInteractions(domainService);
	}

	@Test
	void ifVerifierFailsThenDomainServiceIsNotCalled() {
		OrderController sut = new OrderController(service);
		UUID anyUUID = UUID.fromString("22e1fa97-e3bb-4b1a-8d94-e81d54089fb4");

		String message = "some error mssage";
		when(verifier.verifyAndMap(anyMap(), anyString(), anyString(), anyString(), eq(Order.class)))
				.thenThrow(new RuntimeException(message));

		assertThatRuntimeException()
				.isThrownBy(() -> sut.receive(anyUUID, new SignedPayloadDTO(emptyMap(), "", "", "")))
				.withMessage(message);

		verifyNoMoreInteractions(domainService);
	}

}
