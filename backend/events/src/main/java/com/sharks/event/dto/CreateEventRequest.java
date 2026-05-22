package com.sharks.event.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public class CreateEventRequest {

	@NotBlank
	private String name;

	@NotBlank
	private String address;

	@NotNull
	@Future
	private Instant eventTime;

	@NotNull
	@Positive
	private Integer ticketsProvisioned;

	@NotNull
	@Positive
	private BigDecimal price;

	@Size(max = 4000)
	private String description;

	@NotNull
	private EventType type;

	private String singer;

	private Integer minAge;

	private Integer maxAge;

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

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public Integer getMinAge() {
		return minAge;
	}

	@JsonAlias("min_age")
	public void setMinAge(Integer minAge) {
		this.minAge = minAge;
	}

	public Integer getMaxAge() {
		return maxAge;
	}

	@JsonAlias("max_age")
	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}
}
