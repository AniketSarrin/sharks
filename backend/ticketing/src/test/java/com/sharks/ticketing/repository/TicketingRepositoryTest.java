package com.sharks.ticketing.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.sharks.ticketing.model.Ticket;
import com.sharks.ticketing.model.TicketReceipt;

@DataJpaTest
@EntityScan(basePackageClasses = { Ticket.class, TicketReceipt.class })
@EnableJpaRepositories(basePackageClasses = TicketRepository.class)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class TicketingRepositoryTest {

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private TicketReceiptRepository ticketReceiptRepository;

	@Test
	void ticketCrud() {
		Ticket saved = ticketRepository.save(new Ticket(new BigDecimal("49.99"), 2, 98, 100L));
		assertThat(saved.getId()).isNotNull();

		assertThat(ticketRepository.findById(saved.getId())).isPresent();

		ticketRepository.deleteById(saved.getId());
		assertThat(ticketRepository.findById(saved.getId())).isEmpty();
	}

	@Test
	void ticketReceiptCrud() {
		Ticket ticket = ticketRepository.save(new Ticket(new BigDecimal("10.00"), 0, 10, 1L));
		UUID buyer = UUID.fromString("11111111-1111-1111-1111-111111111111");
		TicketReceipt saved = ticketReceiptRepository.save(new TicketReceipt(
				ticket.getId(),
				buyer,
				1L,
				Instant.parse("2026-01-01T12:00:00Z"),
				1,
				"SHK-TEST-RCPT-1"));
		assertThat(saved.getId()).isNotNull();

		assertThat(ticketReceiptRepository.findById(saved.getId())).isPresent();

		ticketReceiptRepository.deleteById(saved.getId());
		assertThat(ticketReceiptRepository.findById(saved.getId())).isEmpty();
	}

	@Test
	void findReceiptsByUserId() {
		Ticket t1 = ticketRepository.save(new Ticket(new BigDecimal("1.00"), 1, 9, 5L));
		UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		ticketReceiptRepository.save(new TicketReceipt(
				t1.getId(),
				userId,
				5L,
				Instant.now(),
				1,
				"SHK-U7-A"));
		ticketReceiptRepository.save(new TicketReceipt(
				t1.getId(),
				userId,
				5L,
				Instant.now(),
				1,
				"SHK-U7-B"));

		List<TicketReceipt> forUser = ticketReceiptRepository.findByUserIdOrderByIdAsc(userId);
		assertThat(forUser).hasSize(2);
		assertThat(forUser.get(0).getUserId()).isEqualTo(userId);
		assertThat(forUser.get(0).getId()).isLessThan(forUser.get(1).getId());
	}

	@Test
	void soldUnsoldTotalsByEvent() {
		Long eventId = 200L;
		ticketRepository.save(new Ticket(new BigDecimal("25.00"), 10, 90, eventId));
		ticketRepository.save(new Ticket(new BigDecimal("50.00"), 5, 20, eventId));

		EventTicketTotals totals = ticketRepository.getSoldUnsoldTotalsByEventId(eventId);
		assertThat(totals.eventId()).isEqualTo(eventId);
		assertThat(totals.totalSold()).isEqualTo(15L);
		assertThat(totals.totalUnsold()).isEqualTo(110L);
	}

	@Test
	void soldUnsoldTotalsByEvent_whenNoTickets_defaultsToZero() {
		EventTicketTotals totals = ticketRepository.getSoldUnsoldTotalsByEventId(999L);
		assertThat(totals.eventId()).isEqualTo(999L);
		assertThat(totals.totalSold()).isEqualTo(0L);
		assertThat(totals.totalUnsold()).isEqualTo(0L);
	}

	@Test
	void distinctUserIdsForEvent() {
		Long eventId = 300L;
		Ticket ticket = ticketRepository.save(new Ticket(new BigDecimal("15.00"), 3, 7, eventId));
		UUID u1 = UUID.fromString("33333333-3333-3333-3333-333333333333");
		UUID u2 = UUID.fromString("44444444-4444-4444-4444-444444444444");
		ticketReceiptRepository.save(new TicketReceipt(ticket.getId(), u1, eventId, Instant.now(), 1, "SHK-E300-U1"));
		ticketReceiptRepository.save(new TicketReceipt(ticket.getId(), u2, eventId, Instant.now(), 1, "SHK-E300-U2"));
		ticketReceiptRepository.save(new TicketReceipt(ticket.getId(), u2, eventId, Instant.now(), 1, "SHK-E300-U2B"));

		List<UUID> users = ticketReceiptRepository.findDistinctUserIdsByEventId(eventId);
		assertThat(users).containsExactlyInAnyOrder(u1, u2);
	}
}
