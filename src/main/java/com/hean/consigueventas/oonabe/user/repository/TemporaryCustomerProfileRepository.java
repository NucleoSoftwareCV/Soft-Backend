package com.hean.consigueventas.oonabe.user.repository;

import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemporaryCustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
}
