package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventImage;
import com.hean.consigueventas.oonabe.event.repository.EventImageRepository;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.recommendation.dto.response.RecommendedEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
@ExtendWith(MockitoExtension.class)
class EventRecommendationQueryServiceTest {

    private static final String USERNAME = "cliente";

    private static final Instant START =
            Instant.parse("2026-11-01T12:00:00Z");

    private static final Instant END =
            Instant.parse("2026-11-01T13:00:00Z");

    @Mock
    private EventRecommendationService recommendationService;

    @Mock
    private EventImageRepository imageRepository;

    private EventRecommendationQueryService service;

    @BeforeEach
    void setUp() {
        service = new EventRecommendationQueryService(
                recommendationService,
                imageRepository
        );
    }

    @Test
    void shouldPreserveSelectedDatesScoreAndReason() {
        PageRequest pageable = PageRequest.of(0, 12);
        Event event = createEvent(7L);
        event.setMood(com.hean.consigueventas.oonabe.common.enums.EventMood.ENERGIA);

        RecommendedEvent recommendation = new RecommendedEvent(
                event,
                50L,
                START,
                END,
                null,
                40,
                "De un profesional que sigues"
        );

        when(recommendationService.getRecommendations(USERNAME, pageable))
                .thenReturn(new PageImpl<>(
                        List.of(recommendation), pageable, 1
                ));

        when(imageRepository.findOrderedCandidatesByEventIds(List.of(7L)))
                .thenReturn(List.of());

        Page<RecommendedEventResponse> result =
                service.getRecommendations(USERNAME, pageable);

        RecommendedEventResponse response = result.getContent().getFirst();
        assertThat(response.event().mood()).isEqualTo(
                com.hean.consigueventas.oonabe.common.enums.EventMood.ENERGIA);

        assertThat(response.event().id()).isEqualTo(7L);
        assertThat(response.event().title()).isEqualTo("Yoga");
        assertThat(response.event().startsAt()).isEqualTo(START);
        assertThat(response.event().endsAt()).isEqualTo(END);
        assertThat(response.event().categoryId()).isEqualTo(10L);
        assertThat(response.event().organizerId()).isEqualTo(30L);
        assertThat(response.event().cityName()).isNull();
        assertThat(response.event().coverImageUrl()).isNull();
        assertThat(response.event().experienceTypeId()).isNull();

        assertThat(response.occurrenceId()).isEqualTo(50L);
        assertThat(response.event().occurrenceId()).isEqualTo(response.occurrenceId());
        assertThat(response.score()).isEqualTo(40);
        assertThat(response.reason())
                .isEqualTo("De un profesional que sigues");

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldLoadImagesTogetherAndKeepTheFirstOrderedImage() {
        PageRequest pageable = PageRequest.of(0, 2);

        Event firstEvent = createEvent(7L);
        Event secondEvent = createEvent(8L);

        ExperienceType type = new ExperienceType();
        type.setId(20L);
        type.setName("Taller");
        type.setSlug("taller");
        secondEvent.setExperienceType(type);

        RecommendedEvent first = new RecommendedEvent(
                firstEvent, 50L, START, END, null, 40, "Primera razon"
        );

        RecommendedEvent second = new RecommendedEvent(
                secondEvent, 51L, START, END, null, 10, "Segunda razon"
        );

        when(recommendationService.getRecommendations(USERNAME, pageable))
                .thenReturn(new PageImpl<>(
                        List.of(first, second), pageable, 5
                ));

        when(imageRepository.findOrderedCandidatesByEventIds(List.of(7L, 8L)))
                .thenReturn(List.of(
                        image(firstEvent, "/images/portada.jpg"),
                        image(firstEvent, "/images/otra.jpg"),
                        image(secondEvent, "/images/segundo.jpg")
                ));

        Page<RecommendedEventResponse> result =
                service.getRecommendations(USERNAME, pageable);

        assertThat(result.getContent())
                .extracting(response -> response.event().id())
                .containsExactly(7L, 8L);

        assertThat(result.getContent().get(0).event().coverImageUrl())
                .isEqualTo("/images/portada.jpg");

        assertThat(result.getContent().get(1).event().coverImageUrl())
                .isEqualTo("/images/segundo.jpg");

        assertThat(result.getContent().get(1).event().experienceTypeId())
                .isEqualTo(20L);

        assertThat(result.getContent().get(1).event().eventType())
                .isEqualTo("Taller");

        assertThat(result.getTotalElements()).isEqualTo(5);

        verify(imageRepository)
                .findOrderedCandidatesByEventIds(List.of(7L, 8L));

        verifyNoMoreInteractions(imageRepository);
    }

    @Test
    void shouldPreserveEmptyPageMetadataWithoutLoadingImages() {
        PageRequest pageable = PageRequest.of(5, 2);

        when(recommendationService.getRecommendations(USERNAME, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 3));

        Page<RecommendedEventResponse> result =
                service.getRecommendations(USERNAME, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(5);
        assertThat(result.getSize()).isEqualTo(2);

        verifyNoInteractions(imageRepository);
    }

    private Event createEvent(Long id) {
        Category category = new Category();
        category.setId(10L);
        category.setName("Yoga");

        SpecialistProfile specialist = new SpecialistProfile();
        specialist.setId(30L);
        specialist.setPublicName("Ana");

        Event event = new Event();
        event.setId(id);
        event.setTitle("Yoga");
        event.setModality(EventModality.ONLINE);
        event.setCategory(category);
        event.setSpecialist(specialist);

        return event;
    }

    private EventImage image(Event event, String url) {
        EventImage image = new EventImage();
        image.setEvent(event);
        image.setUrl(url);

        return image;
    }
    @Test
    void shouldForwardFiltersAndCityPolicyToRecommendationService() {
        PageRequest pageable = PageRequest.of(0, 12);

        EventFilterRequest filter = mock(EventFilterRequest.class);

        when(recommendationService.getRecommendations(
                USERNAME,
                pageable,
                filter,
                false
        )).thenReturn(Page.empty(pageable));

        var result = service.getRecommendations(
                USERNAME,
                pageable,
                filter,
                false
        );

        assertThat(result.getContent()).isEmpty();

        verify(recommendationService).getRecommendations(
                USERNAME,
                pageable,
                filter,
                false
        );

        verifyNoInteractions(imageRepository);
    }
}