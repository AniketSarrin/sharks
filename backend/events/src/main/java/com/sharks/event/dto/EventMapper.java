package com.sharks.event.dto;

import com.sharks.event.model.DatingEvent;
import com.sharks.event.model.Event;
import com.sharks.event.model.MusicEvent;
import com.sharks.event.model.NetworkingEvent;

import java.util.UUID;

public final class EventMapper {

	private EventMapper() {
	}

	public static Event toEntity(CreateEventRequest req, UUID organizerId) {
		return switch (req.getType()) {
			case MUSIC -> {
				if (req.getSinger() == null || req.getSinger().isBlank()) {
					throw new IllegalArgumentException("singer is required for MUSIC events");
				}
				yield new MusicEvent(
						organizerId,
						req.getName(),
						req.getAddress(),
						req.getEventTime(),
						req.getTicketsProvisioned(),
						req.getDescription(),
						req.getSinger(),
						req.getMinAge(),
						req.getMaxAge());
			}
			case NETWORKING -> new NetworkingEvent(
					organizerId,
					req.getName(),
					req.getAddress(),
					req.getEventTime(),
					req.getTicketsProvisioned(),
					req.getDescription(),
					req.getMinAge(),
					req.getMaxAge());
			case DATING -> new DatingEvent(
					organizerId,
					req.getName(),
					req.getAddress(),
					req.getEventTime(),
					req.getTicketsProvisioned(),
					req.getDescription(),
					req.getMinAge(),
					req.getMaxAge());
		};
	}

	public static EventResponse toResponse(Event event) {
		if (event instanceof MusicEvent m) {
			return new EventResponse(
					event.getId(),
					event.getName(),
					event.getAddress(),
					event.getEventTime(),
					event.getTicketsProvisioned(),
					event.getDescription(),
					EventType.MUSIC,
					event.getOrganizerId(),
					m.getSinger(),
					event.getMinAge(),
					event.getMaxAge(),
					event.getImageUrl());
		}
		if (event instanceof NetworkingEvent) {
			return new EventResponse(
					event.getId(),
					event.getName(),
					event.getAddress(),
					event.getEventTime(),
					event.getTicketsProvisioned(),
					event.getDescription(),
					EventType.NETWORKING,
					event.getOrganizerId(),
					null,
					event.getMinAge(),
					event.getMaxAge(),
					event.getImageUrl());
		}
		if (event instanceof DatingEvent) {
			return new EventResponse(
					event.getId(),
					event.getName(),
					event.getAddress(),
					event.getEventTime(),
					event.getTicketsProvisioned(),
					event.getDescription(),
					EventType.DATING,
					event.getOrganizerId(),
					null,
					event.getMinAge(),
					event.getMaxAge(),
					event.getImageUrl());
		}
		throw new IllegalStateException("Unknown event subtype: " + event.getClass());
	}

	public static void applyUpdate(Event event, UpdateEventRequest req) {
		if (req.getName() != null) {
			event.setName(req.getName());
		}
		if (req.getAddress() != null) {
			event.setAddress(req.getAddress());
		}
		if (req.getEventTime() != null) {
			event.setEventTime(req.getEventTime());
		}
		if (req.getTicketsProvisioned() != null) {
			event.setTicketsProvisioned(req.getTicketsProvisioned());
		}
		if (req.getDescription() != null) {
			event.setDescription(req.getDescription());
		}
		if (event instanceof MusicEvent m && req.getSinger() != null) {
			m.setSinger(req.getSinger());
		}
		if (req.getMinAge() != null) {
			event.setMinAge(req.getMinAge());
		}
		if (req.getMaxAge() != null) {
			event.setMaxAge(req.getMaxAge());
		}
	}
}
