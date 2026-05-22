package com.sharks.event.repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sharks.event.model.Event;

/**
 * Persistence for {@link Event}; callers must save concrete subclasses ({@code MusicEvent}, etc.).
 * <p>
 * Search filters are optional ({@code null} means ignore). Partial matches use case-insensitive
 * substring search on {@code name} and {@code address}. Time range uses {@link Instant} semantics:
 * inclusive lower bound ({@code fromInclusive}), exclusive upper bound ({@code toExclusive}).
 * Maps API &quot;location&quot; to column {@code address}.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

	Page<Event> findByOrganizerIdOrderByEventTimeAsc(UUID organizerId, Pageable pageable);

	String SEARCH_WHERE_FROM = """
			from events e
			where (cast(:nameFragment as varchar) is null \
			  or e.name ilike concat('%', cast(:nameFragment as varchar), '%'))
			  and (cast(:addressFragment as varchar) is null \
			  or e.address ilike concat('%', cast(:addressFragment as varchar), '%'))
			  and (cast(:fromInclusive as timestamptz) is null \
			  or e.event_time >= cast(:fromInclusive as timestamptz))
			  and (cast(:toExclusive as timestamptz) is null \
			  or e.event_time < cast(:toExclusive as timestamptz))
			  and (cast(:eventType as varchar) is null \
			  or e.event_type = cast(:eventType as varchar))
			""";

	String SEARCH_IDS_NATIVE = "select e.id\n" + SEARCH_WHERE_FROM + "order by e.event_time\n";

	String SEARCH_IDS_COUNT = "select count(*)\n" + SEARCH_WHERE_FROM;

	@Query(nativeQuery = true, value = SEARCH_IDS_NATIVE)
	List<Long> searchIds(
			@Param("nameFragment") String nameFragment,
			@Param("addressFragment") String addressFragment,
			@Param("fromInclusive") Instant fromInclusive,
			@Param("toExclusive") Instant toExclusive,
			@Param("eventType") String eventType);

	@Query(nativeQuery = true, value = SEARCH_IDS_NATIVE, countQuery = SEARCH_IDS_COUNT)
	Page<Long> searchIds(
			@Param("nameFragment") String nameFragment,
			@Param("addressFragment") String addressFragment,
			@Param("fromInclusive") Instant fromInclusive,
			@Param("toExclusive") Instant toExclusive,
			@Param("eventType") String eventType,
			Pageable pageable);

	default List<Event> search(
			String nameFragment,
			String addressFragment,
			Instant fromInclusive,
			Instant toExclusive,
			Class<? extends Event> subtype) {
		String eventType = EventDiscriminatorMapper.toDiscriminatorColumn(subtype);
		List<Long> ids = searchIds(nameFragment, addressFragment, fromInclusive, toExclusive, eventType);
		if (ids.isEmpty()) {
			return List.of();
		}
		Map<Long, Event> loaded = EventDiscriminatorMapper.indexById(findAllById(ids));
		return ids.stream().map(loaded::get).filter(Objects::nonNull).toList();
	}

	default Page<Event> search(
			String nameFragment,
			String addressFragment,
			Instant fromInclusive,
			Instant toExclusive,
			Class<? extends Event> subtype,
			Pageable pageable) {
		String eventType = EventDiscriminatorMapper.toDiscriminatorColumn(subtype);
		Page<Long> ids = searchIds(
				nameFragment,
				addressFragment,
				fromInclusive,
				toExclusive,
				eventType,
				pageable == null ? Pageable.unpaged() : pageable);
		List<Long> idList = ids.getContent();
		if (idList.isEmpty()) {
			return new PageImpl<>(List.of(), ids.getPageable(), ids.getTotalElements());
		}
		Map<Long, Event> loaded = EventDiscriminatorMapper.indexById(findAllById(idList));
		List<Event> content = idList.stream().map(loaded::get).filter(Objects::nonNull).toList();
		return new PageImpl<>(content, ids.getPageable(), ids.getTotalElements());
	}
}
