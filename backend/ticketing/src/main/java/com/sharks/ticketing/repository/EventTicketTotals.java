package com.sharks.ticketing.repository;

/**
 * Aggregate sold / unsold inventory for an event (sums {@code tickets.sold} / {@code tickets.unsold}).
 */
public record EventTicketTotals(Long eventId, Long totalSold, Long totalUnsold) {
}
