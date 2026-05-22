package com.sharks.event.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "networking_events")
@DiscriminatorValue("NETWORKING")
@PrimaryKeyJoinColumn(name = "event_id")
public class NetworkingEvent extends Event {

	protected NetworkingEvent() {
		super();
	}

	public NetworkingEvent(
			UUID organizerId,
			String name,
			String address,
			Instant eventTime,
			Integer ticketsProvisioned,
			String description) {
		super(organizerId, name, address, eventTime, ticketsProvisioned, description, null, null);
	}

	public NetworkingEvent(
			UUID organizerId,
			String name,
			String address,
			Instant eventTime,
			Integer ticketsProvisioned,
			String description,
			Integer minAge,
			Integer maxAge) {
		super(organizerId, name, address, eventTime, ticketsProvisioned, description, minAge, maxAge);
	}
}
