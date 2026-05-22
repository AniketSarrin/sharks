package com.sharks.ticketing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ticket_receipts")
public class TicketReceipt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ticket_id", nullable = false)
	private Long ticketId;

	@Column(name = "user_id", nullable = false, columnDefinition = "uuid")
	private UUID userId;

	@Column(name = "event_id", nullable = false)
	private Long eventId;

	@Column(name = "purchased_at", nullable = false)
	private Instant purchasedAt;

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "ticket_code", nullable = false, length = 64)
	private String ticketCode;

	protected TicketReceipt() {
	}

	public TicketReceipt(
			Long ticketId,
			UUID userId,
			Long eventId,
			Instant purchasedAt,
			Integer quantity,
			String ticketCode) {
		this.ticketId = ticketId;
		this.userId = userId;
		this.eventId = eventId;
		this.purchasedAt = purchasedAt;
		this.quantity = quantity;
		this.ticketCode = ticketCode;
	}

	public Long getId() {
		return id;
	}

	public Long getTicketId() {
		return ticketId;
	}

	public void setTicketId(Long ticketId) {
		this.ticketId = ticketId;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public Instant getPurchasedAt() {
		return purchasedAt;
	}

	public void setPurchasedAt(Instant purchasedAt) {
		this.purchasedAt = purchasedAt;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getTicketCode() {
		return ticketCode;
	}

	public void setTicketCode(String ticketCode) {
		this.ticketCode = ticketCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TicketReceipt that)) {
			return false;
		}
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
