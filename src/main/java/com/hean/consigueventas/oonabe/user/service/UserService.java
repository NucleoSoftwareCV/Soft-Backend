package com.hean.consigueventas.oonabe.user.service;

import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.common.exception.UserAlreadyExistsException;
import com.hean.consigueventas.oonabe.profileCliente.service.ClientProfileService;
import com.hean.consigueventas.oonabe.user.dto.response.UserResponse;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.mapper.UserMapper;
import com.hean.consigueventas.oonabe.user.repository.RoleRepository;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryCustomerProfileService temporaryCustomerProfileService;
    private final ClientProfileService clientProfileService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, TemporaryCustomerProfileService temporaryCustomerProfileService, ClientProfileService clientProfileService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.temporaryCustomerProfileService = temporaryCustomerProfileService;
        this.clientProfileService = clientProfileService;
    }

    @Transactional
    public User registerUser(User user, String firstName, String lastName) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("El nombre de usuario ya está en uso.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("El email ya está en uso.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = getOrCreateRole(Role.ROLE_USER, "Usuario final");
        user.setRoles(new HashSet<>(Set.of(userRole)));
        User savedUser = userRepository.save(user);
        temporaryCustomerProfileService.createProfileForUser(savedUser, firstName, lastName);
        clientProfileService.createInitialProfile(savedUser, firstName, lastName);
        return savedUser;
    }

    @Transactional
    public Role getOrCreateRole(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).description(description).active(true).build()));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userMapper.toDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

}
