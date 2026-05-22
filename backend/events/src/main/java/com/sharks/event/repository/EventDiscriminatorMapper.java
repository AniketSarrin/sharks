package com.sharks.event.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.sharks.event.model.DatingEvent;
import com.sharks.event.model.Event;
import com.sharks.event.model.MusicEvent;
import com.sharks.event.model.NetworkingEvent;

final class EventDiscriminatorMapper {

	static final String MUSIC = "MUSIC";
	static final String NETWORKING = "NETWORKING";
	static final String DATING = "DATING";

	private EventDiscriminatorMapper() {
	}

	static String toDiscriminatorColumn(Class<? extends Event> subtype) {
		if (subtype == null) {
			return null;
		}
		if (!Event.class.isAssignableFrom(subtype) || Event.class.equals(subtype)) {
			throw new IllegalArgumentException("subtype must be a concrete Event subclass");
		}
		if (subtype.equals(MusicEvent.class)) {
			return MUSIC;
		}
		if (subtype.equals(NetworkingEvent.class)) {
			return NETWORKING;
		}
		if (subtype.equals(DatingEvent.class)) {
			return DATING;
		}
		throw new IllegalArgumentException("Unhandled Event subtype class: " + subtype.getName());
	}

	static Map<Long, Event> indexById(Iterable<Event> events) {
		Map<Long, Event> map = new HashMap<>();
		for (Event e : events) {
			map.put(Objects.requireNonNull(e.getId()), e);
		}
		return map;
	}
}
