package com.hean.consigueventas.oonabe.profileProfesional.repository;

import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalWorkTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProfessionalWorkTopicRepository
        extends JpaRepository<ProfessionalWorkTopic, Long> {

    //Listar los temas de trabajo asociados al perfil
    List<ProfessionalWorkTopic> findBySpecialistProfileId(
            Long specialistProfileId
    );
    @Query("""
            SELECT pwt FROM ProfessionalWorkTopic pwt
            JOIN FETCH pwt.workTopic
            WHERE pwt.specialistProfile.id IN :profileIds
            """)
    List<ProfessionalWorkTopic> findAllBySpecialistProfileIdIn(
            @Param("profileIds") Collection<Long> profileIds
    );

    //Verificar si el tema ya está asociado al perfil
    boolean existsBySpecialistProfileIdAndWorkTopicId(
            Long specialistProfileId,
            Long workTopicId
    );

    //Eliminar todas las asociaciones del perfil al actualizar
    void deleteBySpecialistProfileId(Long specialistProfileId);
}