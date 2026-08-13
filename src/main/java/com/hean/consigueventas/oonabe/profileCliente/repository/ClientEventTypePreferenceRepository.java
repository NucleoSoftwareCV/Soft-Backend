package com.hean.consigueventas.oonabe.profileCliente.repository;

import com.hean.consigueventas.oonabe.profileCliente.entity.ClientEventTypePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientEventTypePreferenceRepository extends JpaRepository<ClientEventTypePreference, Long> {
    List<ClientEventTypePreference> findByClientProfileId(Long clientProfileId);
    void deleteByClientProfileId(Long clientProfileId);
}
