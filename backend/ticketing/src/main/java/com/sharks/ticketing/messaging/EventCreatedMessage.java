package com.sharks.ticketing.messaging;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload for {@code event.created} messages on {@code sharks.event} / {@code user.event.created}.
 * Matches the events service {@code EventCreatedMessage} JSON shape.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventCreatedMessage(
		Long eventId,
		BigDecimal price,
		Integer ticketsProvisioned
) {
}
