package com.hean.consigueventas.oonabe.home.service;

import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.common.config.TimeConfig;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.event.service.EventCardAssembler;
import com.hean.consigueventas.oonabe.event.specification.EventSpecification;
import com.hean.consigueventas.oonabe.experienceType.repository.ExperienceTypeRepository;
import com.hean.consigueventas.oonabe.home.dto.response.HomeEventSectionFiltersResponse;
import com.hean.consigueventas.oonabe.home.dto.response.HomeEventSectionKey;
import com.hean.consigueventas.oonabe.home.dto.response.HomeEventSectionResponse;
import com.hean.consigueventas.oonabe.home.dto.response.HomeEventSectionsResponse;
import com.hean.consigueventas.oonabe.home.mapper.HomeEventCardMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class HomeEventSectionService {

    private static final String YOGA_SLUG = "yoga";
    private static final String BREATHWORK_SLUG = "hielo-y-breathwork";
    private static final List<String> SLOW_DOWN_SLUGS = List.of(
            "meditacion-y-mindfulness",
            "sonido-y-vibracion",
            "espiritualidad-y-energia"
    );

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final ExperienceTypeRepository experienceTypeRepository;
    private final EventCardAssembler eventCardAssembler;
    private final HomeEventCardMapper homeEventCardMapper;
    private final Clock clock;

    public HomeEventSectionService(
            EventRepository eventRepository,
            CategoryRepository categoryRepository,
            ExperienceTypeRepository experienceTypeRepository,
            EventCardAssembler eventCardAssembler,
            HomeEventCardMapper homeEventCardMapper,
            Clock clock) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.experienceTypeRepository = experienceTypeRepository;
        this.eventCardAssembler = eventCardAssembler;
        this.homeEventCardMapper = homeEventCardMapper;
        this.clock = clock;
    }

    public HomeEventSectionsResponse getEventSections(String cityName, int limit) {
        LocalDate today = LocalDate.now(clock);
        Instant now = clock.instant();
        List<HomeEventSectionResponse> sections = new ArrayList<>();

        HomeDateRanges.DateRange week = HomeDateRanges.remainingWeek(today);
        addIfNotEmpty(sections, querySection(
                HomeEventSectionKey.THIS_WEEK,
                "Esta semana",
                filters(List.of(), null, week, cityName),
                sectionSpecification(cityName, week, now),
                limit
        ));

        HomeDateRanges.DateRange weekend = HomeDateRanges.currentOrNextWeekend(today);
        addIfNotEmpty(sections, querySection(
                HomeEventSectionKey.WEEKEND,
                "Planes para este fin de semana",
                filters(List.of(), null, weekend, cityName),
                sectionSpecification(cityName, weekend, now),
                limit
        ));

        findActiveCategoryId(YOGA_SLUG).ifPresent(categoryId ->
                addIfNotEmpty(sections, queryCategorySection(
                        HomeEventSectionKey.YOGA,
                        "Yoga",
                        List.of(categoryId),
                        today,
                        cityName,
                        now,
                        limit
                )));

        findActiveCategoryId(BREATHWORK_SLUG).ifPresent(categoryId ->
                addIfNotEmpty(sections, queryCategorySection(
                        HomeEventSectionKey.BREATHWORK_ICE,
                        "Breathwork y banos de hielo",
                        List.of(categoryId),
                        today,
                        cityName,
                        now,
                        limit
                )));

        List<Long> slowDownCategoryIds = SLOW_DOWN_SLUGS.stream()
                .map(this::findActiveCategoryId)
                .flatMap(Optional::stream)
                .toList();
        if (!slowDownCategoryIds.isEmpty()) {
            addIfNotEmpty(sections, queryCategorySection(
                    HomeEventSectionKey.SLOW_DOWN,
                    "Para desconectar y bajar el ritmo",
                    slowDownCategoryIds,
                    today,
                    cityName,
                    now,
                    limit
            ));
        }

        findActiveExperienceTypeId("talleres").ifPresent(typeId ->
                addIfNotEmpty(sections, queryTypeSection(
                        HomeEventSectionKey.WORKSHOPS, "Talleres para vivir algo diferente",
                        typeId, today, cityName, now, limit)));
        findActiveExperienceTypeId("retiros").ifPresent(typeId ->
                addIfNotEmpty(sections, queryTypeSection(
                        HomeEventSectionKey.RETREATS, "Retiros e inmersiones",
                        typeId, today, cityName, now, limit)));

        return new HomeEventSectionsResponse(cityName, List.copyOf(sections));
    }

    private HomeEventSectionResponse queryCategorySection(
            HomeEventSectionKey key,
            String title,
            List<Long> categoryIds,
            LocalDate today,
            String cityName,
            Instant now,
            int limit) {
        return querySection(
                key,
                title,
                filters(categoryIds, null, new HomeDateRanges.DateRange(today, null), cityName),
                baseSpecification(cityName, now, null)
                        .and(EventSpecification.hasAnyCategory(categoryIds)),
                limit
        );
    }

    private HomeEventSectionResponse queryTypeSection(
            HomeEventSectionKey key,
            String title,
            Long experienceTypeId,
            LocalDate today,
            String cityName,
            Instant now,
            int limit) {
        return querySection(
                key,
                title,
                filters(List.of(), experienceTypeId, new HomeDateRanges.DateRange(today, null), cityName),
                baseSpecification(cityName, now, null)
                        .and(EventSpecification.hasExperienceType(experienceTypeId)),
                limit
        );
    }

    private HomeEventSectionResponse querySection(
            HomeEventSectionKey key,
            String title,
            HomeEventSectionFiltersResponse filters,
            Specification<Event> specification,
            int limit) {
        List<Event> events = eventRepository.findAll(specification, PageRequest.of(0, limit)).getContent();
        List<EventCardResponse> cards = eventCardAssembler.toCards(events);
        return new HomeEventSectionResponse(
                key,
                title,
                filters,
                cards.stream().map(homeEventCardMapper::toResponse).toList()
        );
    }

    private Specification<Event> baseSpecification(
            String cityName,
            Instant fromInclusive,
            Instant toExclusive) {
        Specification<Event> onlineEvents = EventSpecification.hasModality(EventModality.ONLINE)
                .and(EventSpecification.hasProgrammedOccurrenceBetween(fromInclusive, toExclusive));
        Specification<Event> inPersonEvents = EventSpecification.hasProgrammedOccurrenceInCityBetween(
                cityName,
                fromInclusive,
                toExclusive);

        return EventSpecification.isPublished()
                .and(onlineEvents.or(inPersonEvents))
                .and(EventSpecification.orderByNextProgrammedOccurrence(
                        Sort.Direction.ASC,
                        fromInclusive));
    }

    private Specification<Event> sectionSpecification(
            String cityName,
            HomeDateRanges.DateRange range,
            Instant now) {
        Instant from = range.from().atStartOfDay(TimeConfig.BUSINESS_ZONE).toInstant();
        if (from.isBefore(now)) {
            from = now;
        }
        Instant toExclusive = range.to().plusDays(1)
                .atStartOfDay(TimeConfig.BUSINESS_ZONE)
                .toInstant();
        return baseSpecification(cityName, from, toExclusive);
    }

    private HomeEventSectionFiltersResponse filters(
            List<Long> categoryIds,
            Long experienceTypeId,
            HomeDateRanges.DateRange range,
            String cityName) {
        return new HomeEventSectionFiltersResponse(
                categoryIds,
                experienceTypeId,
                range.from(),
                range.to(),
                cityName,
                true
        );
    }

    private Optional<Long> findActiveCategoryId(String slug) {
        return categoryRepository.findBySlugAndActiveTrue(slug)
                .map(category -> category.getId());
    }

    private Optional<Long> findActiveExperienceTypeId(String slug) {
        return experienceTypeRepository.findBySlugAndActiveTrue(slug).map(type -> type.getId());
    }

    private void addIfNotEmpty(
            List<HomeEventSectionResponse> sections,
            HomeEventSectionResponse section) {
        if (section != null && !section.events().isEmpty()) {
            sections.add(section);
        }
    }
}
