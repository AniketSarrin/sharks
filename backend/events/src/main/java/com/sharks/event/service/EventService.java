package com.sharks.event.service;

import com.sharks.event.dto.CreateEventRequest;
import com.sharks.event.dto.EventMapper;
import com.sharks.event.dto.EventResponse;
import com.sharks.event.dto.UpdateEventRequest;
import com.sharks.event.exception.EventNotFoundException;
import com.sharks.event.exception.ForbiddenOperationException;
import com.sharks.event.model.DatingEvent;
import com.sharks.event.model.Event;
import com.sharks.event.model.MusicEvent;
import com.sharks.event.model.NetworkingEvent;
import com.sharks.event.messaging.EventCancelledMessage;
import com.sharks.event.messaging.EventCancelledPublisher;
import com.sharks.event.messaging.EventCreatedMessage;
import com.sharks.event.messaging.EventCreatedPublisher;
import com.sharks.event.repository.EventRepository;
import com.sharks.event.security.AppRole;
import com.sharks.event.security.AuthPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class EventService {

	private final EventRepository eventRepository;
	private final EventCancelledPublisher eventCancelledPublisher;
	private final EventCreatedPublisher eventCreatedPublisher;
	private final SupabaseStorageService supabaseStorageService;

	public EventService(
			EventRepository eventRepository,
			EventCancelledPublisher eventCancelledPublisher,
			EventCreatedPublisher eventCreatedPublisher,
			SupabaseStorageService supabaseStorageService) {
		this.eventRepository = eventRepository;
		this.eventCancelledPublisher = eventCancelledPublisher;
		this.eventCreatedPublisher = eventCreatedPublisher;
		this.supabaseStorageService = supabaseStorageService;
	}

	@Transactional(readOnly = true)
	public Page<EventResponse> search(String name, String location, LocalDate date, String category, Pageable pageable) {
		Instant fromInclusive = null;
		Instant toExclusive = null;
		if (date != null) {
			fromInclusive = date.atStartOfDay(ZoneOffset.UTC).toInstant();
			toExclusive = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
		}
		String nameFragment = StringUtils.hasText(name) ? name.trim() : null;
		String addressFragment = StringUtils.hasText(location) ? location : null;
		Class<? extends Event> subtype = resolveCategory(category);
		Page<Event> page = eventRepository.search(
				nameFragment, addressFragment, fromInclusive, toExclusive, subtype, pageable);
		return page.map(EventMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public EventResponse getById(Long id) {
		Event event = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		return EventMapper.toResponse(event);
	}

	@Transactional(readOnly = true)
	public Page<EventResponse> findByOrganizerId(UUID organizerId, Pageable pageable) {
		return eventRepository.findByOrganizerIdOrderByEventTimeAsc(organizerId, pageable).map(EventMapper::toResponse);
	}

	/**
	 * Returns events that exist for the requested ids, preserving first-seen order
	 * and omitting duplicates in the input. Unknown ids are skipped.
	 */
	@Transactional(readOnly = true)
	public List<EventResponse> findByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		List<Long> uniqueOrdered = ids.stream().distinct().toList();
		List<Event> found = eventRepository.findAllById(uniqueOrdered);
		Map<Long, Event> byId = new HashMap<>();
		for (Event e : found) {
			byId.put(e.getId(), e);
		}
		return uniqueOrdered.stream().map(byId::get).filter(Objects::nonNull).map(EventMapper::toResponse).toList();
	}

	@Transactional
	public EventResponse create(CreateEventRequest req, MultipartFile image, AuthPrincipal me) {
		Event entity = EventMapper.toEntity(req, me.userId());
		Event saved = eventRepository.save(entity);
		if (image != null && !image.isEmpty()) {
			byte[] bytes;
			try {
				bytes = image.getBytes();
			}
			catch (IOException e) {
				throw new IllegalArgumentException("Failed to read uploaded image", e);
			}
			String url = supabaseStorageService.uploadJpeg(saved.getId(), bytes);
			saved.setImageUrl(url);
			saved = eventRepository.save(saved);
		}
		eventCreatedPublisher.publish(
				new EventCreatedMessage(saved.getId(), req.getPrice(), saved.getTicketsProvisioned()));
		return EventMapper.toResponse(saved);
	}

	@Transactional
	public EventResponse update(Long id, UpdateEventRequest req, AuthPrincipal me) {
		Event event = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		assertCanModify(event, me);
		EventMapper.applyUpdate(event, req);
		Event saved = eventRepository.save(event);
		return EventMapper.toResponse(saved);
	}

	@Transactional
	public void delete(Long id, AuthPrincipal me) {
		Event event = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		assertCanModify(event, me);
		eventRepository.deleteById(id);
		eventCancelledPublisher.publish(new EventCancelledMessage(id, "deleted"));
	}

	private void assertCanModify(Event event, AuthPrincipal me) {
		if (me.role() == AppRole.ADMIN) {
			return;
		}
		if (me.role() != AppRole.ORGANIZER) {
			throw new ForbiddenOperationException("Only admin or organizer may modify events");
		}
		if (!event.getOrganizerId().equals(me.userId())) {
			throw new ForbiddenOperationException("Organizers may only update or delete events they created");
		}
	}

	private static Class<? extends Event> resolveCategory(String category) {
		if (!StringUtils.hasText(category)) {
			return null;
		}
		return switch (category.trim().toUpperCase()) {
			case "MUSIC" -> MusicEvent.class;
			case "NETWORKING" -> NetworkingEvent.class;
			case "DATING" -> DatingEvent.class;
			default -> throw new IllegalArgumentException("Unknown category: " + category);
		};
	}
}
