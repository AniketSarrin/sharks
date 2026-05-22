package com.sharks.event.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload for {@code event.cancelled} messages on {@code sharks.event}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventCancelledMessage(
		Long eventId,
		String reason
) {
}
