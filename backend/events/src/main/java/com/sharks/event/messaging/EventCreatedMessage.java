package com.sharks.event.messaging;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload for {@code event.created} messages on {@code sharks.event}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventCreatedMessage(
		Long eventId,
		BigDecimal price,
		Integer ticketsProvisioned
) {
}
