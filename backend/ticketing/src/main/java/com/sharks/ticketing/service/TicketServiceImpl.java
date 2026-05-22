package com.sharks.ticketing.service;

import com.sharks.ticketing.dto.PurchaseTicketRequest;
import com.sharks.ticketing.dto.TicketReceiptResponse;
import com.sharks.ticketing.exception.InsufficientInventoryException;
import com.sharks.ticketing.exception.TicketNotFoundException;
import com.sharks.ticketing.messaging.EventCreatedMessage;
import com.sharks.ticketing.model.Ticket;
import com.sharks.ticketing.model.TicketReceipt;
import com.sharks.ticketing.repository.TicketReceiptRepository;
import com.sharks.ticketing.repository.TicketRepository;
import com.sharks.ticketing.security.AuthPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

	private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

	private static final String STATUS_CONFIRMED = "confirmed";

	private final TicketRepository ticketRepository;
	private final TicketReceiptRepository ticketReceiptRepository;

	public TicketServiceImpl(TicketRepository ticketRepository, TicketReceiptRepository ticketReceiptRepository) {
		this.ticketRepository = ticketRepository;
		this.ticketReceiptRepository = ticketReceiptRepository;
	}

	@Override
	@Transactional
	public TicketReceiptResponse purchase(PurchaseTicketRequest request, AuthPrincipal principal) {
		Ticket ticket = resolveTicket(request);
		if (ticket.getUnsold() < request.getQuantity()) {
			throw new InsufficientInventoryException("Not enough tickets available for this tier");
		}
		ticket.setUnsold(ticket.getUnsold() - request.getQuantity());
		ticket.setSold(ticket.getSold() + request.getQuantity());
		ticketRepository.save(ticket);

		Instant now = Instant.now();
		String ticketCode = generateTicketCode();
		TicketReceipt receipt = new TicketReceipt(
				ticket.getId(),
				principal.userId(),
				ticket.getEventId(),
				now,
				request.getQuantity(),
				ticketCode);
		TicketReceipt saved = ticketReceiptRepository.save(receipt);
		return toResponse(saved, ticket);
	}

	@Override
	@Transactional
	public void provisionFromEventCreated(EventCreatedMessage message) {
		if (message == null) {
			log.warn("Ignoring null event.created message");
			return;
		}
		if (message.eventId() == null || message.price() == null || message.ticketsProvisioned() == null) {
			log.warn("Ignoring event.created with missing fields: {}", message);
			return;
		}
		if (message.ticketsProvisioned() <= 0) {
			log.warn("Ignoring event.created with non-positive ticketsProvisioned: eventId={}", message.eventId());
			return;
		}
		if (!ticketRepository.findByEventIdOrderByIdAsc(message.eventId()).isEmpty()) {
			log.info("Skipping duplicate event.created for eventId={}", message.eventId());
			return;
		}
		Ticket ticket = new Ticket(message.price(), 0, message.ticketsProvisioned(), message.eventId());
		ticketRepository.save(ticket);
		log.info("Provisioned ticket inventory for eventId={}, unsold={}", message.eventId(), message.ticketsProvisioned());
	}

	@Override
	@Transactional(readOnly = true)
	public List<TicketReceiptResponse> getMyTickets(AuthPrincipal principal) {
		List<TicketReceipt> receipts = ticketReceiptRepository.findByUserIdOrderByIdAsc(principal.userId());
		return mapReceipts(receipts);
	}

	private Ticket resolveTicket(PurchaseTicketRequest request) {
		if (request.getTicketId() != null) {
			Ticket ticket = ticketRepository.findById(request.getTicketId())
					.orElseThrow(() -> new TicketNotFoundException("Ticket not found"));
			if (!Objects.equals(ticket.getEventId(), request.getEventId())) {
				throw new IllegalArgumentException("ticketId does not belong to the given eventId");
			}
			return ticket;
		}
		List<Ticket> byEvent = ticketRepository.findByEventIdOrderByIdAsc(request.getEventId());
		if (byEvent.isEmpty()) {
			throw new TicketNotFoundException("No ticket inventory for this event");
		}
		if (byEvent.size() > 1) {
			throw new IllegalArgumentException("Multiple ticket tiers exist for this event; specify ticketId");
		}
		return byEvent.get(0);
	}

	private List<TicketReceiptResponse> mapReceipts(List<TicketReceipt> receipts) {
		if (receipts.isEmpty()) {
			return List.of();
		}
		Set<Long> ticketIds = receipts.stream().map(TicketReceipt::getTicketId).collect(Collectors.toSet());
		Map<Long, Ticket> tickets = ticketRepository.findAllById(ticketIds).stream()
				.collect(Collectors.toMap(Ticket::getId, t -> t));
		return receipts.stream()
				.map(r -> {
					Ticket ticket = tickets.get(r.getTicketId());
					if (ticket == null) {
						throw new TicketNotFoundException("Ticket not found for receipt " + r.getId());
					}
					return toResponse(r, ticket);
				})
				.toList();
	}

	private static TicketReceiptResponse toResponse(TicketReceipt receipt, Ticket ticket) {
		BigDecimal unit = ticket.getPrice();
		BigDecimal total = unit.multiply(BigDecimal.valueOf(receipt.getQuantity()));
		return new TicketReceiptResponse(
				receipt.getId(),
				receipt.getEventId(),
				receipt.getTicketId(),
				receipt.getQuantity(),
				unit,
				total,
				receipt.getTicketCode(),
				receipt.getPurchasedAt(),
				STATUS_CONFIRMED);
	}

	private static String generateTicketCode() {
		String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
		return "SHK-" + suffix;
	}
}
