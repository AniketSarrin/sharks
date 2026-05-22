package com.sharks.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.sharks.event.model.DatingEvent;
import com.sharks.event.model.Event;
import com.sharks.event.model.MusicEvent;
import com.sharks.event.model.NetworkingEvent;
import com.sharks.event.repository.EventRepository;

@DataJpaTest(properties = {"spring.jpa.show-sql=false"})
@EntityScan(basePackageClasses = Event.class)
@EnableJpaRepositories(basePackageClasses = EventRepository.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class EventRepositoryTest {

	@SuppressWarnings("resource")
	@Container
	static PostgreSQLContainer<?> postgres =
			new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
					.withDatabaseName("events")
					.withUsername("postgres")
					.withPassword("postgres");

	@SuppressWarnings("unused")
	@DynamicPropertySource
	static void dataSourceProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private EventRepository eventRepository;

	private static final UUID ORG = UUID.fromString("11111111-1111-1111-1111-111111111111");

	private static Instant t(String iso) {
		return Instant.parse(iso);
	}

	@Test
	void crud_saveFindDelete_networkingSubtype() {
		NetworkingEvent e =
				new NetworkingEvent(ORG, "Tech Mixer", "san jose 1st ave", t("2026-06-01T20:00:00Z"), 120, "Meet engineers");
		eventRepository.save(e);

		Long id = e.getId();
		assertThat(id).isNotNull();

		Event loaded = eventRepository.findById(id).orElseThrow();
		assertThat(loaded).isInstanceOf(NetworkingEvent.class);
		assertThat(loaded.getName()).isEqualTo("Tech Mixer");
		assertThat(loaded.getAddress()).containsIgnoringCase("San Jose");

		eventRepository.deleteById(id);
		assertThat(eventRepository.findById(id)).isEmpty();
	}

	@Test
	void crud_saveFind_datingSubtype_persistsAgeRange() {
		DatingEvent e =
				new DatingEvent(
						ORG,
						"Age-gated Mixer",
						"100 First St",
						t("2026-08-15T19:00:00Z"),
						80,
						"For young professionals",
						25,
						40);
		eventRepository.save(e);

		Event loaded = eventRepository.findById(e.getId()).orElseThrow();
		assertThat(loaded).isInstanceOf(DatingEvent.class);
		DatingEvent d = (DatingEvent) loaded;
		assertThat(d.getMinAge()).isEqualTo(25);
		assertThat(d.getMaxAge()).isEqualTo(40);
	}

	@Test
	void crud_saveFind_musicSubtype_persistsAgeRange() {
		MusicEvent e =
				new MusicEvent(
						ORG,
						"Ladies Night Concert",
						"Civic Auditorium",
						t("2026-10-01T01:00:00Z"),
						600,
						"Pop hits",
						"DJ Example",
						21,
						45);
		eventRepository.save(e);

		Event loaded = eventRepository.findById(e.getId()).orElseThrow();
		assertThat(loaded).isInstanceOf(MusicEvent.class);
		assertThat(loaded.getMinAge()).isEqualTo(21);
		assertThat(loaded.getMaxAge()).isEqualTo(45);
	}

	@Test
	void crud_saveFind_musicSubtype() {
		MusicEvent e =
				new MusicEvent(
						ORG,
						"Summer Gala",
						"Municipal Auditorium 100 Main St",
						t("2026-07-10T02:30:00Z"),
						500,
						"Vocal ensemble",
						"Ada Lovelace");
		eventRepository.save(e);

		Event loaded = eventRepository.findById(e.getId()).orElseThrow();
		assertThat(loaded).isInstanceOf(MusicEvent.class);
		assertThat(((MusicEvent) loaded).getSinger()).isEqualTo("Ada Lovelace");
	}

	@Test
	void search_partialNameAndAddress_andDateRange_andCategory() {
		eventRepository.save(
				new NetworkingEvent(ORG, "Coffee Networking", "123 Market Street", t("2026-05-03T14:00:00Z"), 40, null));
		eventRepository.save(
				new MusicEvent(ORG, "Coffee Concert", "456 Elm Road", t("2026-05-04T16:00:00Z"), 300, null, "Sam"));

		List<Event> byNameFrag = eventRepository.search("coffee", null, null, null, null);
		assertThat(byNameFrag).hasSizeGreaterThanOrEqualTo(2);

		List<Event> byAddr = eventRepository.search(null, "market", null, null, null);
		assertThat(byAddr).extracting(Event::getName).anyMatch(n -> n.contains("Coffee Networking"));

		List<Event> inRange =
				eventRepository.search(null, null, t("2026-05-03T13:00:00Z"), t("2026-05-04T02:00:00Z"), null);
		assertThat(inRange.stream().map(Event::getName).toList()).containsExactly("Coffee Networking");

		List<Event> musicOnly = eventRepository.search(null, null, null, null, MusicEvent.class);
		assertThat(musicOnly.stream().map(Event::getName).toList()).contains("Coffee Concert");
	}

	@Test
	void findByOrganizerId_filtersAndOrdersByEventTime() {
		UUID otherOrg = UUID.fromString("22222222-2222-2222-2222-222222222222");
		eventRepository.save(
				new NetworkingEvent(ORG, "Org First", "Addr", t("2026-11-02T12:00:00Z"), 10, null));
		eventRepository.save(
				new NetworkingEvent(ORG, "Org Second", "Addr", t("2026-11-01T12:00:00Z"), 10, null));
		eventRepository.save(
				new NetworkingEvent(otherOrg, "Other Org", "Addr", t("2026-11-03T12:00:00Z"), 10, null));

		Page<Event> page =
				eventRepository.findByOrganizerIdOrderByEventTimeAsc(ORG, PageRequest.of(0, 10));
		assertThat(page.getContent()).hasSize(2);
		assertThat(page.getContent().stream().map(Event::getName).toList())
				.containsExactly("Org Second", "Org First");
	}

	@Test
	void search_pagedPreservesSliceSize() {
		eventRepository.save(
				new NetworkingEvent(ORG, "Paged A", "UniqueLocxyz", t("2026-09-01T10:00:00Z"), 10, null));
		eventRepository.save(
				new NetworkingEvent(ORG, "Paged B", "UniqueLocxyz", t("2026-09-02T11:00:00Z"), 10, null));

		Page<Event> page = eventRepository.search(
				null,
				"UniqueLocxyz",
				null,
				null,
				NetworkingEvent.class,
				PageRequest.of(0, 1));
		assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
		assertThat(page.getContent()).hasSize(1);
		assertThat(page.getContent().get(0).getName()).startsWith("Paged");
	}
}
