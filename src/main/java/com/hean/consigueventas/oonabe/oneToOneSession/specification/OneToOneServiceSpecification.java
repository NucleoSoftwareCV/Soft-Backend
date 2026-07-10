package com.hean.consigueventas.oonabe.oneToOneSession.specification;

import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class OneToOneServiceSpecification {

    private OneToOneServiceSpecification() {
    }

    public static Specification<OneToOneService> publicListing(String search, Long workTopicId, Long techniqueId) {
        return published()
                .and(matchesSearch(search))
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

    private static Specification<OneToOneService> matchesSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            query.distinct(true);
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            var specialist = root.join("specialist", JoinType.LEFT);
            var workTopics = root.join("workTopics", JoinType.LEFT);
            var techniques = root.join("techniques", JoinType.LEFT);

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(specialist.get("publicName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(workTopics.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(techniques.get("name")), pattern)
            );
        };
    }
}
