package com.sharks.ticketing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.jdbc.Sql;

import com.sharks.ticketing.dto.PurchaseTicketRequest;
import com.sharks.ticketing.dto.TicketReceiptResponse;
import com.sharks.ticketing.exception.InsufficientInventoryException;
import com.sharks.ticketing.exception.TicketNotFoundException;
import com.sharks.ticketing.model.Ticket;
import com.sharks.ticketing.model.TicketReceipt;
import com.sharks.ticketing.security.AppRole;
import com.sharks.ticketing.security.AuthPrincipal;

@DataJpaTest
@EntityScan(basePackageClasses = { Ticket.class, TicketReceipt.class })
@EnableJpaRepositories(basePackageClasses = com.sharks.ticketing.repository.TicketRepository.class)
@Import(TicketServiceImpl.class)
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Sql(statements = {
		"delete from ticket_receipts",
		"delete from tickets"
})
class TicketServiceTest {

	private static final UUID BUYER = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

	@Autowired
	private TicketService ticketService;

	@Autowired
	private com.sharks.ticketing.repository.TicketRepository ticketRepository;

	@Test
	void purchase_reservesInventory_andCreatesReceipt() {
		Ticket tier = ticketRepository.save(new Ticket(new BigDecimal("25.00"), 0, 5, 10L));

		PurchaseTicketRequest req = new PurchaseTicketRequest();
		req.setEventId(10L);
		req.setQuantity(2);

		TicketReceiptResponse res = ticketService.purchase(req, new AuthPrincipal(BUYER, AppRole.ATTENDEE));

		assertThat(res.quantity()).isEqualTo(2);
		assertThat(res.totalPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
		assertThat(res.ticketCode()).startsWith("SHK-");
		assertThat(res.status()).isEqualTo("confirmed");

		Ticket reloaded = ticketRepository.findById(tier.getId()).orElseThrow();
		assertThat(reloaded.getSold()).isEqualTo(2);
		assertThat(reloaded.getUnsold()).isEqualTo(3);
	}

	@Test
	void purchase_whenAmbiguousTiers_requiresTicketId() {
		ticketRepository.save(new Ticket(new BigDecimal("10.00"), 0, 5, 20L));
		ticketRepository.save(new Ticket(new BigDecimal("15.00"), 0, 5, 20L));

		PurchaseTicketRequest req = new PurchaseTicketRequest();
		req.setEventId(20L);
		req.setQuantity(1);

		assertThatThrownBy(() -> ticketService.purchase(req, new AuthPrincipal(BUYER, AppRole.ATTENDEE)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("ticketId");
	}

	@Test
	void purchase_whenNotEnoughInventory_throws() {
		Ticket tier = ticketRepository.save(new Ticket(new BigDecimal("5.00"), 0, 1, 30L));

		PurchaseTicketRequest req = new PurchaseTicketRequest();
		req.setEventId(30L);
		req.setTicketId(tier.getId());
		req.setQuantity(3);

		assertThatThrownBy(() -> ticketService.purchase(req, new AuthPrincipal(BUYER, AppRole.ATTENDEE)))
				.isInstanceOf(InsufficientInventoryException.class);
	}

	@Test
	void getMyTickets_returnsUserReceipts() {
		Ticket tier = ticketRepository.save(new Ticket(new BigDecimal("3.00"), 0, 10, 40L));
		PurchaseTicketRequest req = new PurchaseTicketRequest();
		req.setEventId(40L);
		req.setQuantity(1);
		ticketService.purchase(req, new AuthPrincipal(BUYER, AppRole.ATTENDEE));

		List<TicketReceiptResponse> mine = ticketService.getMyTickets(new AuthPrincipal(BUYER, AppRole.ATTENDEE));
		assertThat(mine).hasSize(1);
		assertThat(mine.get(0).eventId()).isEqualTo(40L);
	}

	@Test
	void purchase_unknownTier_throwsNotFound() {
		PurchaseTicketRequest req = new PurchaseTicketRequest();
		req.setEventId(99L);
		req.setTicketId(9_999_999L);
		req.setQuantity(1);

		assertThatThrownBy(() -> ticketService.purchase(req, new AuthPrincipal(BUYER, AppRole.ATTENDEE)))
				.isInstanceOf(TicketNotFoundException.class);
	}

	@Test
	void purchase_ticketIdEventMismatch_throwsBadRequest() {
		Ticket tier = ticketRepository.save(new Ticket(new BigDecimal("8.00"), 0, 2, 50L));

		PurchaseTicketRequest req = new PurchaseTicketRequest();
		req.setEventId(999L);
		req.setTicketId(tier.getId());
		req.setQuantity(1);

		assertThatThrownBy(() -> ticketService.purchase(req, new AuthPrincipal(BUYER, AppRole.ATTENDEE)))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
