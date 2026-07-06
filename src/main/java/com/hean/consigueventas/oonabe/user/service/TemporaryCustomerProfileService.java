package com.hean.consigueventas.oonabe.user.service;

import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.TemporaryCustomerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TemporaryCustomerProfileService {

    private final TemporaryCustomerProfileRepository temporaryCustomerProfileRepository;

    public TemporaryCustomerProfileService(TemporaryCustomerProfileRepository temporaryCustomerProfileRepository) {
        this.temporaryCustomerProfileRepository = temporaryCustomerProfileRepository;
    }

    @Transactional
    public void createProfileForUser(User user) {
        CustomerProfile profile = new CustomerProfile();
        profile.setUser(user);
        profile.setFirstNames(user.getUsername());
        profile.setLastNames("");
        temporaryCustomerProfileRepository.save(profile);
    }
}
