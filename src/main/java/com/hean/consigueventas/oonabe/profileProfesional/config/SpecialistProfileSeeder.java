package com.hean.consigueventas.oonabe.profileProfesional.config;

import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SpecialistProfileSeeder {

    private final SpecialistProfileRepository specialistProfileRepository;
    private final CityRepository cityRepository;

    public SpecialistProfileSeeder(
            SpecialistProfileRepository specialistProfileRepository,
            CityRepository cityRepository
    ) {
        this.specialistProfileRepository = specialistProfileRepository;
        this.cityRepository = cityRepository;
    }

    // Sobrecarga historica sin ciudad: mantiene compatibilidad con llamadas
    // existentes (el perfil se crea sin ciudad y el frontend muestra el
    // texto de respaldo).
    @Transactional
    public SpecialistProfile seedSpecialist(
            User user,
            String slug,
            String publicName,
            String biography,
            String description,
            String photoUrl,
            String whatsappPhone,
            String publicEmail,
            String website) {
        return seedSpecialist(
                user,
                slug,
                publicName,
                biography,
                description,
                photoUrl,
                whatsappPhone,
                publicEmail,
                website,
                null
        );
    }

    // Siembra (o actualiza) un perfil profesional asignandole una ciudad por
    // nombre (debe existir en el catalogo de ciudades, p. ej. "Valencia",
    // "Barcelona", "Madrid").
    @Transactional
    public SpecialistProfile seedSpecialist(
            User user,
            String slug,
            String publicName,
            String biography,
            String description,
            String photoUrl,
            String whatsappPhone,
            String publicEmail,
            String website,
            String cityName) {
        City city = resolveCity(cityName);
        SpecialistProfile profile = specialistProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            SpecialistProfile newProfile = new SpecialistProfile();
            newProfile.setUser(user);
            newProfile.setSlug(slug);
            newProfile.setPublicName(publicName);
            newProfile.setBiography(biography);
            newProfile.setDescription(description);
            newProfile.setPhotoUrl(photoUrl);
            newProfile.setWhatsappPhone(whatsappPhone);
            newProfile.setPublicEmail(publicEmail);
            newProfile.setWebsite(website);
            newProfile.setCity(city);
            newProfile.setApprovalStatus(ApprovalStatus.APROBADO);
            newProfile.setPublicationStatus(PublicationStatus.PUBLICADO);
            return specialistProfileRepository.save(newProfile);
        } else {
            boolean changed = false;
            if (!slug.equals(profile.getSlug())) { profile.setSlug(slug); changed = true; }
            if (!publicName.equals(profile.getPublicName())) { profile.setPublicName(publicName); changed = true; }
            if (!biography.equals(profile.getBiography())) { profile.setBiography(biography); changed = true; }
            if (!description.equals(profile.getDescription())) { profile.setDescription(description); changed = true; }
            if (!photoUrl.equals(profile.getPhotoUrl())) { profile.setPhotoUrl(photoUrl); changed = true; }
            if (!whatsappPhone.equals(profile.getWhatsappPhone())) { profile.setWhatsappPhone(whatsappPhone); changed = true; }
            if (!publicEmail.equals(profile.getPublicEmail())) { profile.setPublicEmail(publicEmail); changed = true; }
            if (website != null && !website.equals(profile.getWebsite())) { profile.setWebsite(website); changed = true; }
            if (city != null && !city.equals(profile.getCity())) { profile.setCity(city); changed = true; }
            if (profile.getApprovalStatus() != ApprovalStatus.APROBADO) { profile.setApprovalStatus(ApprovalStatus.APROBADO); changed = true; }
            if (profile.getPublicationStatus() != PublicationStatus.PUBLICADO) { profile.setPublicationStatus(PublicationStatus.PUBLICADO); changed = true; }
            if (changed) {
                return specialistProfileRepository.save(profile);
            }
            return profile;
        }
    }

    private City resolveCity(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return null;
        }

        String name = cityName.trim();
        return cityRepository
                .findByNameAndProvince(name, name)
                .orElse(null);
    }
}
