package com.hean.consigueventas.oonabe.event.support;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

public final class EventPageables {

    private static final int MAX_RELATED_PAGE_SIZE = 20;
    private static final Set<String> ALLOWED_RELATED_SORT_PROPERTIES = Set.of("startsAt", "createdAt", "title", "priceFrom");
    private static final Sort DEFAULT_RELATED_SORT = Sort.by(Sort.Order.asc("startsAt"));

    private EventPageables() {
    }

    public static Pageable sanitizeRelatedListing(Pageable pageable) {
        Sort safeSort = DEFAULT_RELATED_SORT;

        if (pageable.getSort().isSorted()) {
            List<Sort.Order> validOrders = pageable.getSort().stream()
                    .filter(order -> ALLOWED_RELATED_SORT_PROPERTIES.contains(order.getProperty()))
                    .toList();
            if (!validOrders.isEmpty()) {
                safeSort = Sort.by(validOrders);
            }
        }

        int safeSize = Math.min(pageable.getPageSize(), MAX_RELATED_PAGE_SIZE);
        return PageRequest.of(pageable.getPageNumber(), safeSize, safeSort);
    }
}
