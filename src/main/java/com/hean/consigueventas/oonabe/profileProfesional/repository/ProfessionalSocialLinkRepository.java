package com.hean.consigueventas.oonabe.profileProfesional.repository;

import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalSocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalSocialLinkRepository
        extends JpaRepository<ProfessionalSocialLink, Long> {

    //Listar las redes sociales asociadas al perfil
    List<ProfessionalSocialLink> findBySpecialistProfileId(
            Long specialistProfileId
    );

    List<ProfessionalSocialLink> findBySpecialistProfileIdIn(
            Collection<Long> specialistProfileIds
    );

    //Buscar una plataforma específica dentro del perfil
    Optional<ProfessionalSocialLink>
    findBySpecialistProfileIdAndPlatformIgnoreCase(
            Long specialistProfileId,
            String platform
    );

    //Evitar registrar dos veces la misma plataforma
    boolean existsBySpecialistProfileIdAndPlatformIgnoreCase(
            Long specialistProfileId,
            String platform
    );
}