package com.hean.consigueventas.oonabe.profileProfesional.repository;

import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalLanguageRepository
        extends JpaRepository<ProfessionalLanguage, Long> {

    List<ProfessionalLanguage> findBySpecialistProfileIdOrderByLanguageNameAsc(
            Long specialistProfileId
    );

    List<ProfessionalLanguage>
    findBySpecialistProfileIdInOrderByLanguageNameAsc(
            Collection<Long> specialistProfileIds
    );

    Optional<ProfessionalLanguage> findBySpecialistProfileIdAndLanguageNameIgnoreCase(
            Long specialistProfileId,
            String languageName
    );

    Optional<ProfessionalLanguage> findBySpecialistProfileIdAndId(
            Long specialistProfileId,
            Long id
    );

    void deleteBySpecialistProfileIdAndId(
            Long specialistProfileId,
            Long id
    );
}