package com.hean.consigueventas.oonabe.user.config;

import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.RoleRepository;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserSeeder(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public Role seedRole(String name, String description) {
        Role role = roleRepository.findByName(name).orElse(null);
        if (role == null) {
            Role newRole = Role.builder()
                    .name(name)
                    .description(description)
                    .active(true)
                    .build();
            return roleRepository.save(newRole);
        } else {
            boolean changed = false;
            if (!description.equals(role.getDescription())) {
                role.setDescription(description);
                changed = true;
            }
            if (!role.isActive()) {
                role.setActive(true);
                changed = true;
            }
            if (changed) {
                return roleRepository.save(role);
            }
            return role;
        }
    }

    @Transactional
    public User seedUser(String username, String email, String password, Set<Role> roles) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRoles(new HashSet<>(roles));
            newUser.setActive(true);
            return userRepository.save(newUser);
        } else {
            boolean changed = false;
            if (!email.equals(user.getEmail())) {
                user.setEmail(email);
                changed = true;
            }
            if (!password.equals(user.getPassword())) {
                user.setPassword(password);
                changed = true;
            }
            Set<String> currentRoleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
            Set<String> targetRoleNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
            if (!currentRoleNames.equals(targetRoleNames)) {
                user.setRoles(new HashSet<>(roles));
                changed = true;
            }
            if (!user.isActive()) {
                user.setActive(true);
                changed = true;
            }
            if (changed) {
                return userRepository.save(user);
            }
            return user;
        }
    }
}
