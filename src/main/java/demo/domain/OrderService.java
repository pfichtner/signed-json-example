package demo.domain;

import java.util.UUID;

/** A trivial domain service that would contain business rules. */
public class OrderService {

	public void create(UUID id, Order order) {
		// For demo, just print
		System.out.println("Processed order #" + id + ": " + order);
	}

}
