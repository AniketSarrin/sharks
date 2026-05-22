package com.sharks.event.controller;

import com.sharks.event.dto.CreateEventRequest;
import com.sharks.event.dto.EventIdsRequest;
import com.sharks.event.dto.EventResponse;
import com.sharks.event.dto.UpdateEventRequest;
import com.sharks.event.security.AuthPrincipal;
import com.sharks.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

	private final EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	/**
	 * GET /api/events?name=&location=&date=&category= - Search events (public).
	 * {@code name} is optional; when set, matches event title by case-insensitive substring.
	 */
	@GetMapping
	public Page<EventResponse> getEvents(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String location,
			@RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) String category,
			Pageable pageable) {
		return eventService.search(name, location, date, category, pageable);
	}

	/**
	 * GET /api/events/organizer/{organizerId} - List events created by organizer (public, paged).
	 */
	@GetMapping("/organizer/{organizerId}")
	public Page<EventResponse> getEventsByOrganizer(
			@PathVariable UUID organizerId, Pageable pageable) {
		return eventService.findByOrganizerId(organizerId, pageable);
	}

	/**
	 * GET /api/events/{id} - Get single event (public).
	 */
	@GetMapping("/{id}")
	public EventResponse getEvent(@PathVariable Long id) {
		return eventService.getById(id);
	}

	/**
	 * POST /api/events/by-ids - Resolve many events by id (public). Body field
	 * {@code ids}: list of positive longs (max 500). Duplicate ids are ignored; missing ids are omitted.
	 */
	@PostMapping("/by-ids")
	public List<EventResponse> getEventsByIds(@Valid @RequestBody EventIdsRequest body) {
		return eventService.findByIds(body.ids());
	}

	/**
	 * POST /api/events - Create event (admin or organizer).
	 * {@code multipart/form-data}: part {@code event} is JSON ({@link CreateEventRequest});
	 * optional part {@code image} is a JPEG file.
	 */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
	public EventResponse createEvent(
			@Valid @RequestPart("event") CreateEventRequest request,
			@RequestPart(value = "image", required = false) MultipartFile image,
			@AuthenticationPrincipal AuthPrincipal principal) {
		return eventService.create(request, image, principal);
	}

	/**
	 * PUT /api/events/{id} - Update event (admin any; organizer own only).
	 */
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
	public EventResponse updateEvent(
			@PathVariable Long id,
			@Valid @RequestBody UpdateEventRequest request,
			@AuthenticationPrincipal AuthPrincipal principal) {
		return eventService.update(id, request, principal);
	}

	/**
	 * DELETE /api/events/{id} - Delete event (admin any; organizer own only).
	 */
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
	public ResponseEntity<Void> deleteEvent(
			@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
		eventService.delete(id, principal);
		return ResponseEntity.noContent().build();
	}
}
