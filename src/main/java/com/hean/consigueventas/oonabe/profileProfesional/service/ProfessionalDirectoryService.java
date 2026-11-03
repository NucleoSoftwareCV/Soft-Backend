package com.hean.consigueventas.oonabe.profileProfesional.service;

import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalDirectoryResponse;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalDirectoryResponse.Profile;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalDirectoryResponse.FeaturedService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProfessionalDirectoryService {
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    public ProfessionalDirectoryService(NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    // Consultas agrupadas, sin solicitar una API o consulta por cada tarjeta.
    // Las adquisiciones cuentan reservas CONFIRMADA/COMPLETADA, no carritos ni pendientes.
    private static final String CTE = """
        WITH public_profiles AS (
            SELECT p.* FROM specialist_profiles p JOIN users u ON u.id = p.user_id
            WHERE p.approval_status = 'APROBADO' AND p.publication_status = 'PUBLICADO'
              AND u.status = 'ACTIVO' AND u.deleted_at IS NULL
        ), taxonomy AS (
            SELECT pt.specialist_profile_id AS specialist_id, t.name
            FROM professional_techniques pt JOIN techniques t ON t.id = pt.technique_id
            WHERE t.active = true
            UNION
            SELECT pw.specialist_profile_id, w.name
            FROM professional_work_topics pw JOIN work_topics w ON w.id = pw.work_topic_id
            WHERE w.active = true
            UNION
            SELECT e.specialist_id, c.name FROM events e JOIN categories c ON c.id = e.category_id
            WHERE e.status = 'PUBLICADO'
            UNION
            SELECT s.specialist_id, t.name FROM one_to_one_services s
            JOIN one_to_one_service_techniques st ON st.one_to_one_service_id = s.id
            JOIN techniques t ON t.id = st.technique_id
            WHERE s.status = 'PUBLICADO' AND t.active = true
            UNION
            SELECT s.specialist_id, w.name FROM one_to_one_services s
            JOIN one_to_one_service_work_topics sw ON sw.one_to_one_service_id = s.id
            JOIN work_topics w ON w.id = sw.work_topic_id
            WHERE s.status = 'PUBLICADO' AND w.active = true
        ), event_ratings AS (
            SELECT event_id, AVG(stars)::double precision AS average_rating, COUNT(*) AS review_count
            FROM event_reviews GROUP BY event_id
        ), profile_ratings AS (
            SELECT e.specialist_id, AVG(r.stars)::double precision AS average_rating, COUNT(*) AS review_count
            FROM event_reviews r JOIN events e ON e.id = r.event_id GROUP BY e.specialist_id
        ), event_sales AS (
            SELECT o.event_id, COUNT(*) AS acquisitions
            FROM event_bookings b JOIN event_occurrences o ON o.id = b.occurrence_id
            WHERE b.status IN ('CONFIRMADA', 'COMPLETADA') GROUP BY o.event_id
        ), session_sales AS (
            SELECT service_id, COUNT(*) AS acquisitions FROM session_bookings
            WHERE status IN ('CONFIRMADA', 'COMPLETADA') GROUP BY service_id
        ), catalog AS (
            SELECT e.id, e.specialist_id, 'EVENT' AS type, e.title,
                   COALESCE(e.summary, '') AS summary, e.modality, c.name AS category,
                   e.starting_price AS price, e.currency, upcoming.starts_at,
                   COALESCE(sales.acquisitions, 0) AS acquisitions,
                   rating.average_rating, COALESCE(rating.review_count, 0) AS review_count,
                   (SELECT i.url FROM event_images i WHERE i.event_id = e.id
                    ORDER BY i.cover DESC, i.sort_order, i.id LIMIT 1) AS image_url
            FROM events e JOIN public_profiles p ON p.id = e.specialist_id
            JOIN categories c ON c.id = e.category_id
            JOIN LATERAL (
                SELECT MIN(o.starts_at) AS starts_at FROM event_occurrences o
                WHERE o.event_id = e.id AND o.status = 'PROGRAMADA'
                  AND o.starts_at >= :now AND o.capacity > o.reserved_spots
            ) upcoming ON true
            LEFT JOIN event_sales sales ON sales.event_id = e.id
            LEFT JOIN event_ratings rating ON rating.event_id = e.id
            WHERE e.status = 'PUBLICADO' AND p.show_upcoming_events = true
            UNION ALL
            SELECT s.id, s.specialist_id, 'SESSION', s.title, s.description, s.modality,
                   'Sesión 1:1', s.price, s.currency, NULL::timestamptz,
                   COALESCE(sales.acquisitions, 0), NULL::double precision, 0::bigint, s.image_url
            FROM one_to_one_services s JOIN public_profiles p ON p.id = s.specialist_id
            LEFT JOIN session_sales sales ON sales.service_id = s.id
            WHERE s.status = 'PUBLICADO' AND p.show_one_to_one_sessions = true
        ), offerings AS (
            SELECT * FROM catalog WHERE type = 'SESSION' OR starts_at IS NOT NULL
        ), profile_cities AS (
            SELECT DISTINCT s.specialist_id, c.name
            FROM one_to_one_services s JOIN offerings f ON f.id = s.id AND f.type = 'SESSION'
            JOIN locations l ON l.id = s.location_id JOIN cities c ON c.id = l.city_id
            UNION
            SELECT DISTINCT e.specialist_id, c.name
            FROM events e JOIN offerings f ON f.id = e.id AND f.type = 'EVENT'
            JOIN event_occurrences o ON o.event_id = e.id
            JOIN locations l ON l.id = o.location_id JOIN cities c ON c.id = l.city_id
            WHERE o.status = 'PROGRAMADA' AND o.starts_at >= :now
        ), directory AS (
            SELECT p.id, p.slug, p.public_name, p.profile_category, p.biography, p.photo_url,
                   ARRAY(SELECT t.name FROM taxonomy t WHERE t.specialist_id = p.id ORDER BY t.name) AS specialties,
                   ARRAY(SELECT c.name FROM profile_cities c WHERE c.specialist_id = p.id ORDER BY c.name) AS city_names,
                   cheapest.price, cheapest.currency, cheapest.starts_at, cheapest.type AS cheapest_type,
                   rating.average_rating, COALESCE(rating.review_count, 0) AS review_count
            FROM public_profiles p
            LEFT JOIN profile_ratings rating ON rating.specialist_id = p.id
            LEFT JOIN LATERAL (
                SELECT f.* FROM offerings f WHERE f.specialist_id = p.id AND f.price IS NOT NULL
                ORDER BY CASE WHEN f.currency = 'EUR' THEN 0 ELSE 1 END, f.currency, f.price, f.starts_at NULLS LAST, f.type, f.id
                LIMIT 1
            ) cheapest ON true
        ), filtered AS (
            SELECT d.* FROM directory d
            WHERE ((:type = 'centers' AND UPPER(d.profile_category) IN ('CENTRO', 'CENTROS'))
              OR (:type = 'professionals' AND UPPER(d.profile_category) NOT IN ('CENTRO', 'CENTROS')))
              AND (:search = '' OR CONCAT_WS(' ', d.public_name, d.biography, array_to_string(d.specialties, ' ')) ILIKE :searchPattern)
              AND (:location = '' OR array_to_string(d.city_names, ' ') ILIKE :locationPattern)
              AND (:specialty = '' OR :specialty = ANY(d.specialties))
        )
        """;

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public ProfessionalDirectoryResponse get(String search, String location, String type,
                                             String specialty, int page, int size) {
        var params = new MapSqlParameterSource()
                .addValue("now", java.sql.Timestamp.from(clock.instant()))
                .addValue("search", search).addValue("searchPattern", "%" + escapeLike(search) + "%")
                .addValue("location", location).addValue("locationPattern", "%" + escapeLike(location) + "%")
                .addValue("type", type).addValue("specialty", specialty)
                .addValue("limit", size).addValue("offset", (long) page * size);
        Long total = jdbc.queryForObject(CTE + " SELECT COUNT(*) FROM filtered", params, Long.class);
        List<Profile> profiles = jdbc.query(CTE + " SELECT * FROM filtered ORDER BY LOWER(public_name), id LIMIT :limit OFFSET :offset",
                params, (rs, row) -> new Profile(rs.getLong("id"), rs.getString("slug"), rs.getString("public_name"),
                        rs.getString("profile_category"), rs.getString("biography"), rs.getString("photo_url"),
                        strings(rs, "specialties"), strings(rs, "city_names"), rs.getBigDecimal("price"),
                        rs.getString("currency"), instant(rs, "starts_at"), rs.getString("cheapest_type"),
                        rs.getObject("average_rating", Double.class), rs.getLong("review_count")));
        List<String> specialties = jdbc.query(CTE + """
            SELECT DISTINCT t.name FROM taxonomy t JOIN public_profiles p ON p.id = t.specialist_id
            WHERE ((:type = 'centers' AND UPPER(p.profile_category) IN ('CENTRO', 'CENTROS'))
              OR (:type = 'professionals' AND UPPER(p.profile_category) NOT IN ('CENTRO', 'CENTROS')))
              AND TRIM(t.name) <> ''
            ORDER BY t.name
            """,
                params, (rs, row) -> rs.getString("name"));
        List<FeaturedService> featured = jdbc.query(CTE + """
            SELECT f.*, p.public_name FROM catalog f JOIN filtered p ON p.id = f.specialist_id
            WHERE f.acquisitions > 0 OR f.review_count > 0
            ORDER BY f.acquisitions DESC, f.average_rating DESC NULLS LAST, f.review_count DESC,
                     f.starts_at NULLS LAST, f.type, f.id LIMIT 1
            """, params, (rs, row) -> new FeaturedService(rs.getLong("id"), rs.getString("type"),
                rs.getString("title"), rs.getString("summary"), rs.getString("image_url"),
                rs.getString("public_name"), rs.getString("modality"), rs.getString("category"),
                rs.getBigDecimal("price"), rs.getString("currency"), instant(rs, "starts_at"),
                rs.getLong("acquisitions"), rs.getObject("average_rating", Double.class), rs.getLong("review_count")));
        return new ProfessionalDirectoryResponse(profiles, total == null ? 0 : total, page, size,
                specialties, featured.isEmpty() ? null : featured.getFirst());
    }

    private static List<String> strings(ResultSet rs, String column) throws SQLException {
        var array = rs.getArray(column);
        if (array == null) return List.of();
        try { return Arrays.asList((String[]) array.getArray()); }
        finally { array.free(); }
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        var value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
