package com.hean.consigueventas.oonabe.profileProfesional.repository;

import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalImageRepository
        extends JpaRepository<ProfessionalImage, Long> {

    //Listar las imágenes de un perfil ordenadas para su visualización
    List<ProfessionalImage>
    findBySpecialistProfileIdOrderByDisplayOrderAsc(Long specialistProfileId);

    //Buscar el banner de un perfil, cuando exista
    Optional<ProfessionalImage>
    findBySpecialistProfileIdAndBannerTrue(Long specialistProfileId);

    //Verificar si el perfil ya tiene un banner registrado
    boolean existsBySpecialistProfileIdAndBannerTrue(Long specialistProfileId);
}