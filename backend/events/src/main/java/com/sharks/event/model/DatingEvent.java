package com.sharks.event.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dating_events")
@DiscriminatorValue("DATING")
@PrimaryKeyJoinColumn(name = "event_id")
public class DatingEvent extends Event {

	protected DatingEvent() {
		super();
	}

	public DatingEvent(
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
