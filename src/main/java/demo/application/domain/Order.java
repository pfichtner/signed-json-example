package demo.application.domain;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Price {
		double amount;
		String currency;
	}

	UUID id;
	String name;
	Price price;

}
