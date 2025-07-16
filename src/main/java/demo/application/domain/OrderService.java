package demo.application.domain;

import java.util.UUID;

import org.springframework.stereotype.Service;

/** A trivial domain service that would contain business rules. */
@Service
public class OrderService {

	public void create(UUID id, Order order) {
		// For demo, just print
		System.out.println("Processed order #" + id + ": " + order);
	}

}
