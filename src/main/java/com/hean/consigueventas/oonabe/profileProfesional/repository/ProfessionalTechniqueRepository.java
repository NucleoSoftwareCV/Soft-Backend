package com.hean.consigueventas.oonabe.profileProfesional.repository;

import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalTechnique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProfessionalTechniqueRepository
        extends JpaRepository<ProfessionalTechnique, Long> {

    //Listar las técnicas asociadas al perfil
    List<ProfessionalTechnique> findBySpecialistProfileId(
            Long specialistProfileId
    );

    @Query("""
            SELECT pt FROM ProfessionalTechnique pt
            JOIN FETCH pt.technique
            WHERE pt.specialistProfile.id IN :profileIds
            """)
    List<ProfessionalTechnique> findAllBySpecialistProfileIdIn(
            @Param("profileIds") Collection<Long> profileIds
    );

    //Verificar si la técnica ya está asociada al perfil
    boolean existsBySpecialistProfileIdAndTechniqueId(
            Long specialistProfileId,
            Long techniqueId
    );

    //Eliminar todas las técnicas asociadas al actualizar el perfil
    void deleteBySpecialistProfileId(Long specialistProfileId);
}