package com.sharks.event.dto;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
		Long id,
		String name,
		String address,
		Instant eventTime,
		Integer ticketsProvisioned,
		String description,
		EventType type,
		UUID organizerId,
		String singer,
		Integer minAge,
		Integer maxAge,
		String imageUrl) {
}
