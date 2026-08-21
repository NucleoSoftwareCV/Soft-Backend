package com.hean.consigueventas.oonabe.profileCliente.repository;

import com.hean.consigueventas.oonabe.profileCliente.entity.ClientModalityPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientModalityPreferenceRepository extends JpaRepository<ClientModalityPreference, Long> {
    Optional<ClientModalityPreference> findByClientProfileId(Long clientProfileId);
}
