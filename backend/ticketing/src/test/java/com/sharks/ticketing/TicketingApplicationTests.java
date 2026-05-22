package com.sharks.ticketing;

import com.sharks.ticketing.repository.TicketReceiptRepository;
import com.sharks.ticketing.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
		properties = {
				"spring.autoconfigure.exclude="
						+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
						+ "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
						+ "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
		})
class TicketingApplicationTests {

	@MockitoBean
	private TicketRepository ticketRepository;

	@MockitoBean
	private TicketReceiptRepository ticketReceiptRepository;

	@Test
	void contextLoads() {
	}

}
