package com.sharks.ticketing.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sharks.ticketing.model.TicketReceipt;

public interface TicketReceiptRepository extends JpaRepository<TicketReceipt, Long> {

	List<TicketReceipt> findByUserIdOrderByIdAsc(UUID userId);

	@Query("select r.userId from TicketReceipt r where r.eventId = :eventId")
	List<UUID> findUserIdsByEventId(@Param("eventId") Long eventId);

	default List<UUID> findDistinctUserIdsByEventId(Long eventId) {
		return findUserIdsByEventId(eventId).stream().distinct().sorted().toList();
	}
}
