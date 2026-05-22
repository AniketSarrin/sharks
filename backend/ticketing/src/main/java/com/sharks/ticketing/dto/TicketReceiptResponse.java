package com.sharks.ticketing.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TicketReceiptResponse(
		Long id,
		Long eventId,
		Long ticketId,
		int quantity,
		BigDecimal unitPrice,
		BigDecimal totalPrice,
		String ticketCode,
		Instant purchasedAt,
		String status) {
}
