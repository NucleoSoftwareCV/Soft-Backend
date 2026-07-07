package com.hean.consigueventas.oonabe.oneToOneSession.specification;

import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import org.springframework.data.jpa.domain.Specification;

public final class OneToOneServiceSpecification {

    private OneToOneServiceSpecification() {
    }

    public static Specification<OneToOneService> publicListing(Long workTopicId, Long techniqueId) {
        return published()
                .and(hasWorkTopic(workTopicId))
                .and(hasTechnique(techniqueId));
    }

    private static Specification<OneToOneService> published() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), PublicationStatus.PUBLICADO);
    }

    private static Specification<OneToOneService> hasWorkTopic(Long workTopicId) {
        return (root, query, criteriaBuilder) -> {
            if (workTopicId == null) {
                return criteriaBuilder.conjunction();
            }
            query.distinct(true);
            return criteriaBuilder.equal(root.join("workTopics").get("id"), workTopicId);
        };
    }

    private static Specification<OneToOneService> hasTechnique(Long techniqueId) {
        return (root, query, criteriaBuilder) -> {
            if (techniqueId == null) {
                return criteriaBuilder.conjunction();
            }
            query.distinct(true);
            return criteriaBuilder.equal(root.join("techniques").get("id"), techniqueId);
        };
    }
}
