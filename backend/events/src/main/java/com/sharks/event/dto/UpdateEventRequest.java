package com.sharks.event.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class UpdateEventRequest {

	private String name;

	private String address;

	@Future
	private Instant eventTime;

	@Positive
	private Integer ticketsProvisioned;

	@Size(max = 4000)
	private String description;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
