package com.hean.consigueventas.oonabe.profileCliente.repository;

import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientProfileRepository
        extends JpaRepository<ClientProfile, Long> {

    Optional<ClientProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}