package com.hean.consigueventas.oonabe.profileProfesional.repository;

import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalTechnique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfessionalTechniqueRepository
        extends JpaRepository<ProfessionalTechnique, Long> {

    //Listar las técnicas asociadas al perfil
    List<ProfessionalTechnique> findBySpecialistProfileId(
            Long specialistProfileId
    );

    //Verificar si la técnica ya está asociada al perfil
    boolean existsBySpecialistProfileIdAndTechniqueId(
            Long specialistProfileId,
            Long techniqueId
    );

    //Eliminar todas las técnicas asociadas al actualizar el perfil
    void deleteBySpecialistProfileId(Long specialistProfileId);
}