package com.hean.consigueventas.oonabe.profileProfesional.repository;

import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecialistProfileRepository
        extends JpaRepository<SpecialistProfile, Long> {

    //Buscar el perfil mediante el ID del usuario relacionado
    Optional<SpecialistProfile> findByUserId(Long userId);

    //Buscar un perfil por su slug o enlace identificador
    Optional<SpecialistProfile> findBySlug(String slug);

    //Buscar un perfil por su slug cuando está aprobado y publicado
    Optional<SpecialistProfile> findBySlugAndApprovalStatusAndPublicationStatus(
            String slug,
            ApprovalStatus approvalStatus,
            PublicationStatus publicationStatus
    );

    //Validar que el slug no esté registrado
    boolean existsBySlug(String slug);

    //Validar que el slug no pertenezca a otro perfil al actualizar
    boolean existsBySlugAndIdNot(String slug, Long id);

    //Listar perfiles según su estado de aprobación y publicación
    //Aprobación: PENDIENTE, APROBADO o RECHAZADO
    //Publicación: BORRADOR o PUBLICADO
    Page<SpecialistProfile> findByApprovalStatusAndPublicationStatus(
            ApprovalStatus approvalStatus,
            PublicationStatus publicationStatus,
            Pageable pageable
    );

    //Filtrar perfiles públicos por categoría, aprobación y publicación
    Page<SpecialistProfile>
    findByProfileCategoryIgnoreCaseAndApprovalStatusAndPublicationStatus(
            String profileCategory,
            ApprovalStatus approvalStatus,
            PublicationStatus publicationStatus,
            Pageable pageable
    );

    //Filtrar perfiles pendientes - aprobados - rechazados
    Page<SpecialistProfile> findByApprovalStatus(
            ApprovalStatus approvalStatus,
            Pageable pageable
    );

    //Filtrar perfiles publicados - borradores
    Page<SpecialistProfile> findByPublicationStatus(
            PublicationStatus publicationStatus,
            Pageable pageable
    );

    //Buscar perfiles publicos por texto libre (nombre publico o biografia),
    //opcionalmente filtrando por categoria de perfil
    @Query("""
            SELECT p FROM SpecialistProfile p
            WHERE p.approvalStatus = :approvalStatus
              AND p.publicationStatus = :publicationStatus
              AND (:profileCategory IS NULL OR LOWER(p.profileCategory) = :profileCategory)
              AND (
                LOWER(p.publicName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.biography) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<SpecialistProfile> searchPublicProfiles(
            @Param("profileCategory") String profileCategory,
            @Param("approvalStatus") ApprovalStatus approvalStatus,
            @Param("publicationStatus") PublicationStatus publicationStatus,
            @Param("search") String search,
            Pageable pageable
    );
}