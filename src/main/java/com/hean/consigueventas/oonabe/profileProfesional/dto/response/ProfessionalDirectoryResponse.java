package com.hean.consigueventas.oonabe.profileProfesional.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Datos públicos del directorio; no expone usuarios, reservas ni datos de contacto privados. */
public record ProfessionalDirectoryResponse(
        List<Profile> content, long totalElements, int number, int size,
        List<String> specialties, FeaturedService featured) {
    public record Profile(
            Long id, String slug, String publicName, String profileCategory,
            String biography, String photoUrl, List<String> specialties,
            List<String> cityNames, BigDecimal priceFrom, String currency,
            Instant nextStartsAt, String cheapestServiceType,
            Double averageRating, long reviewCount) {}

    public record FeaturedService(
            Long id, String type, String title, String summary, String imageUrl,
            String specialistName, String modality, String category,
            BigDecimal price, String currency, Instant startsAt,
            long acquisitionCount, Double averageRating, long reviewCount) {}
}