package com.sharks.event;

import com.sharks.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
		properties = {
				"spring.autoconfigure.exclude="
						+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
						+ "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
						+ "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration,"
						+ "org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration"
		})
class EventApplicationTests {

	@MockitoBean
	private EventRepository eventRepository;

	@MockitoBean
	private ConnectionFactory connectionFactory;

	@Test
	void contextLoads() {
	}

}
