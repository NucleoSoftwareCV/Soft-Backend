package com.hean.consigueventas.oonabe.profileProfesional.repository;

import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecialistProfileRepository extends JpaRepository<SpecialistProfile, Long> {
    Optional<SpecialistProfile> findByUserId(Long userId);
    Optional<SpecialistProfile> findBySlug(String slug);
}
