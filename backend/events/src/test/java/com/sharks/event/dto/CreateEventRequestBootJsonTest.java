package com.sharks.event.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

/**
 * Uses Spring Boot’s auto-configured Jackson 3 mapper (same stack as {@code @RestController} bodies).
 */
@JsonTest
class CreateEventRequestBootJsonTest {

	@Autowired
	private JacksonTester<CreateEventRequest> json;

	@Test
	void deserializes_snake_case_age_fields() throws Exception {
		String body =
				"""
				{
				  "name": "Singles Night",
				  "address": "1 Main St",
				  "eventTime": "2030-06-01T20:00:00Z",
				  "ticketsProvisioned": 50,
				  "price": 29.99,
				  "type": "DATING",
				  "min_age": 21,
				  "max_age": 35
				}
				""";
		CreateEventRequest req = json.parse(body).getObject();
		assertThat(req.getMinAge()).isEqualTo(21);
		assertThat(req.getMaxAge()).isEqualTo(35);
		assertThat(req.getPrice()).isEqualByComparingTo("29.99");
	}

	@Test
	void deserializes_camel_case_age_fields() throws Exception {
		String body =
				"""
				{
				  "name": "Singles Night",
				  "address": "1 Main St",
				  "eventTime": "2030-06-01T20:00:00Z",
				  "ticketsProvisioned": 50,
				  "price": 15,
				  "type": "DATING",
				  "minAge": 22,
				  "maxAge": 40
				}
				""";
		CreateEventRequest req = json.parse(body).getObject();
		assertThat(req.getMinAge()).isEqualTo(22);
		assertThat(req.getMaxAge()).isEqualTo(40);
		assertThat(req.getPrice()).isEqualByComparingTo("15");
	}
}
