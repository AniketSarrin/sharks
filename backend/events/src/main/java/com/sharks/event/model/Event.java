package com.sharks.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "events")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "event_type")
public abstract class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String address;

	@Column(name = "event_time", nullable = false)
	private Instant eventTime;

	@Column(name = "tickets_provisioned", nullable = false)
	private Integer ticketsProvisioned;

	@Column(length = 4000)
	private String description;

	@Column(name = "organizer_id", nullable = false, updatable = false)
	private UUID organizerId;

	@Column(name = "min_age")
	private Integer minAge;

	@Column(name = "max_age")
	private Integer maxAge;

	@Column(name = "image_url")
	private String imageUrl;

	protected Event() {
	}

	protected Event(
			UUID organizerId,
			String name,
			String address,
			Instant eventTime,
			Integer ticketsProvisioned,
			String description,
			Integer minAge,
			Integer maxAge) {
		this.organizerId = organizerId;
		this.name = name;
		this.address = address;
		this.eventTime = eventTime;
		this.ticketsProvisioned = ticketsProvisioned;
		this.description = description;
		this.minAge = minAge;
		this.maxAge = maxAge;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Instant getEventTime() {
		return eventTime;
	}

	public void setEventTime(Instant eventTime) {
		this.eventTime = eventTime;
	}

	public Integer getTicketsProvisioned() {
		return ticketsProvisioned;
	}

	public void setTicketsProvisioned(Integer ticketsProvisioned) {
		this.ticketsProvisioned = ticketsProvisioned;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UUID getOrganizerId() {
		return organizerId;
	}

	public Integer getMinAge() {
		return minAge;
	}

	public void setMinAge(Integer minAge) {
		this.minAge = minAge;
	}

	public Integer getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Event event)) {
			return false;
		}
		return id != null && Objects.equals(id, event.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
