package com.sharks.ticketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sharks.ticketing.model.Ticket;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	List<Ticket> findByEventIdOrderByIdAsc(Long eventId);

	@Query("select sum(t.sold) from Ticket t where t.eventId = :eventId")
	Long sumSoldForEvent(@Param("eventId") Long eventId);

	@Query("select sum(t.unsold) from Ticket t where t.eventId = :eventId")
	Long sumUnsoldForEvent(@Param("eventId") Long eventId);

	default EventTicketTotals getSoldUnsoldTotalsByEventId(Long eventId) {
		Long totalSold = sumSoldForEvent(eventId);
		Long totalUnsold = sumUnsoldForEvent(eventId);
		return new EventTicketTotals(
				eventId,
				totalSold != null ? totalSold : 0L,
				totalUnsold != null ? totalUnsold : 0L);
	}
}
