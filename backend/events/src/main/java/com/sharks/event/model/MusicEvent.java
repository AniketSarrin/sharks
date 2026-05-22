package com.sharks.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "music_events")
@DiscriminatorValue("MUSIC")
@PrimaryKeyJoinColumn(name = "event_id")
public class MusicEvent extends Event {

	@Column(nullable = false)
	private String singer;

	protected MusicEvent() {
		super();
	}

	public MusicEvent(
			UUID organizerId,
			String name,
			String address,
			Instant eventTime,
			Integer ticketsProvisioned,
			String description,
			String singer,
			Integer minAge,
			Integer maxAge) {
		super(organizerId, name, address, eventTime, ticketsProvisioned, description, minAge, maxAge);
		this.singer = singer;
	}

	/** Backward-compatible constructor (no age limits). */
	public MusicEvent(
			UUID organizerId,
			String name,
			String address,
			Instant eventTime,
			Integer ticketsProvisioned,
			String description,
			String singer) {
		this(organizerId, name, address, eventTime, ticketsProvisioned, description, singer, null, null);
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}
}
