package com.sharks.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.sharks.event.dto.CreateEventRequest;
import com.sharks.event.dto.EventType;
import com.sharks.event.messaging.EventCancelledPublisher;
import com.sharks.event.messaging.EventCreatedMessage;
import com.sharks.event.messaging.EventCreatedPublisher;
import com.sharks.event.model.NetworkingEvent;
import com.sharks.event.repository.EventRepository;
import com.sharks.event.security.AppRole;
import com.sharks.event.security.AuthPrincipal;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

	private static final UUID ORG = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Mock
	private EventRepository eventRepository;

	@Mock
	private EventCancelledPublisher eventCancelledPublisher;

	@Mock
	private EventCreatedPublisher eventCreatedPublisher;

	@Mock
	private SupabaseStorageService supabaseStorageService;

	@InjectMocks
	private EventService eventService;

	@Test
	void findByIds_preservesOrder_skipsMissing_dedupesInput() {
		NetworkingEvent a = networking("A");
		NetworkingEvent b = networking("B");
		ReflectionTestUtils.setField(a, "id", 1L);
		ReflectionTestUtils.setField(b, "id", 2L);
		when(eventRepository.findAllById(List.of(2L, 999L, 1L))).thenReturn(List.of(b, a));

		assertThat(eventService.findByIds(List.of(2L, 999L, 1L, 2L)))
				.hasSize(2)
				.extracting(r -> r.id())
				.containsExactly(2L, 1L);

		verify(eventRepository).findAllById(List.of(2L, 999L, 1L));
	}

	@Test
	void search_passesTrimmedNameAsFirstArgumentToRepository() {
		Pageable pageable = PageRequest.of(0, 20);
		when(eventRepository.search(eq("Summer"), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of()));

		eventService.search("  Summer  ", null, null, null, pageable);

		verify(eventRepository).search(eq("Summer"), isNull(), isNull(), isNull(), isNull(), eq(pageable));
	}

	@Test
	void create_publishesEventCreatedMessage_afterSave() {
		CreateEventRequest req = new CreateEventRequest();
		req.setName("Summer Fest");
		req.setAddress("addr");
		req.setEventTime(Instant.parse("2030-06-01T20:00:00Z"));
		req.setTicketsProvisioned(100);
		req.setPrice(new BigDecimal("29.99"));
		req.setType(EventType.NETWORKING);

		when(eventRepository.save(any(NetworkingEvent.class))).thenAnswer(invocation -> {
			NetworkingEvent e = invocation.getArgument(0);
			ReflectionTestUtils.setField(e, "id", 42L);
			return e;
		});

		assertThat(eventService.create(req, null, new AuthPrincipal(ORG, AppRole.ADMIN)).id()).isEqualTo(42L);

		ArgumentCaptor<EventCreatedMessage> captor = ArgumentCaptor.forClass(EventCreatedMessage.class);
		verify(eventCreatedPublisher).publish(captor.capture());
		EventCreatedMessage msg = captor.getValue();
		assertThat(msg.eventId()).isEqualTo(42L);
		assertThat(msg.price()).isEqualByComparingTo("29.99");
		assertThat(msg.ticketsProvisioned()).isEqualTo(100);
		verifyNoInteractions(eventCancelledPublisher);
		verifyNoInteractions(supabaseStorageService);
	}

	private static NetworkingEvent networking(String name) {
		return new NetworkingEvent(ORG, name, "addr", Instant.parse("2026-06-01T20:00:00Z"), 10, null);
	}
}
