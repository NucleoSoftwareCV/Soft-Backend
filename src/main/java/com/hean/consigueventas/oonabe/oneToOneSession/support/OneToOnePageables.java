package com.hean.consigueventas.oonabe.oneToOneSession.support;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

public final class OneToOnePageables {

    private static final int MAX_PUBLIC_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_PUBLIC_SORT_PROPERTIES = Set.of("createdAt", "title", "price", "durationMinutes");
    private static final Sort DEFAULT_PUBLIC_SORT = Sort.by(Sort.Order.desc("createdAt"));

    private OneToOnePageables() {
    }

    public static Pageable sanitizePublicListing(Pageable pageable) {
        Sort safeSort = DEFAULT_PUBLIC_SORT;

        if (pageable.getSort().isSorted()) {
            List<Sort.Order> validOrders = pageable.getSort().stream()
                    .filter(order -> ALLOWED_PUBLIC_SORT_PROPERTIES.contains(order.getProperty()))
                    .toList();
            if (!validOrders.isEmpty()) {
                safeSort = Sort.by(validOrders);
            }
        }

        int safeSize = Math.min(pageable.getPageSize(), MAX_PUBLIC_PAGE_SIZE);
        return PageRequest.of(pageable.getPageNumber(), safeSize, safeSort);
    }
}
