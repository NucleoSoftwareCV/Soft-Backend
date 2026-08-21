package com.hean.consigueventas.oonabe.profileProfesional.config;

import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SpecialistProfileSeeder {

    private final SpecialistProfileRepository specialistProfileRepository;

    public SpecialistProfileSeeder(SpecialistProfileRepository specialistProfileRepository) {
        this.specialistProfileRepository = specialistProfileRepository;
    }

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
            if (profile.getApprovalStatus() != ApprovalStatus.APROBADO) { profile.setApprovalStatus(ApprovalStatus.APROBADO); changed = true; }
            if (profile.getPublicationStatus() != PublicationStatus.PUBLICADO) { profile.setPublicationStatus(PublicationStatus.PUBLICADO); changed = true; }
            if (changed) {
                return specialistProfileRepository.save(profile);
            }
            return profile;
        }
    }
}
