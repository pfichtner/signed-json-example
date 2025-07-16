package demo.application.domain;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import demo.application.domain.Order.Price;

class OrderServiceTest {

	OrderService service = new OrderService();

	@Test
	void processOrder() {
		UUID id = randomUUID();
		Order order = new Order(id, new Price(10, "EUR"));
		assertThatCode(() -> service.create(id, order)).doesNotThrowAnyException();
	}

}
