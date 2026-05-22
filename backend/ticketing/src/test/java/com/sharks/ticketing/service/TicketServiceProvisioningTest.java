package com.sharks.ticketing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sharks.ticketing.messaging.EventCreatedMessage;
import com.sharks.ticketing.model.Ticket;
import com.sharks.ticketing.repository.TicketReceiptRepository;
import com.sharks.ticketing.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class TicketServiceProvisioningTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketReceiptRepository ticketReceiptRepository;

	@InjectMocks
	private TicketServiceImpl ticketService;

	@Test
	void provisionFromEventCreated_persistsTicket_withSoldZero_andUnsoldProvisioned() {
		when(ticketRepository.findByEventIdOrderByIdAsc(100L)).thenReturn(List.of());

		ticketService.provisionFromEventCreated(
				new EventCreatedMessage(100L, new BigDecimal("42.50"), 80));

		ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
		verify(ticketRepository).save(captor.capture());
		Ticket saved = captor.getValue();
		assertThat(saved.getEventId()).isEqualTo(100L);
		assertThat(saved.getPrice()).isEqualByComparingTo("42.50");
		assertThat(saved.getSold()).isEqualTo(0);
		assertThat(saved.getUnsold()).isEqualTo(80);
	}

	@Test
	void provisionFromEventCreated_skips_whenInventoryAlreadyExistsForEvent() {
		when(ticketRepository.findByEventIdOrderByIdAsc(200L))
				.thenReturn(List.of(new Ticket(new BigDecimal("1.00"), 0, 5, 200L)));

		ticketService.provisionFromEventCreated(
				new EventCreatedMessage(200L, new BigDecimal("9.99"), 10));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void provisionFromEventCreated_skips_whenTicketsProvisionedNotPositive() {
		ticketService.provisionFromEventCreated(
				new EventCreatedMessage(300L, new BigDecimal("1.00"), 0));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void provisionFromEventCreated_skips_whenMessageNull() {
		ticketService.provisionFromEventCreated(null);

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void provisionFromEventCreated_skips_whenRequiredFieldsMissing() {
		ticketService.provisionFromEventCreated(
				new EventCreatedMessage(null, new BigDecimal("1.00"), 5));

		verify(ticketRepository, never()).save(any());
	}
}
