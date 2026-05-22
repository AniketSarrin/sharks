package com.sharks.ticketing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "tickets")
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private Integer sold;

	@Column(nullable = false)
	private Integer unsold;

	@Column(name = "event_id", nullable = false)
	private Long eventId;

	protected Ticket() {
	}

	public Ticket(BigDecimal price, Integer sold, Integer unsold, Long eventId) {
		this.price = price;
		this.sold = sold;
		this.unsold = unsold;
		this.eventId = eventId;
	}

	public Long getId() {
		return id;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getSold() {
		return sold;
	}

	public void setSold(Integer sold) {
		this.sold = sold;
	}

	public Integer getUnsold() {
		return unsold;
	}

	public void setUnsold(Integer unsold) {
		this.unsold = unsold;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Ticket ticket)) {
			return false;
		}
		return id != null && Objects.equals(id, ticket.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
