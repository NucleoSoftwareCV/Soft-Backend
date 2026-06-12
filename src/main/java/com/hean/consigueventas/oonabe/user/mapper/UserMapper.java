package com.hean.consigueventas.oonabe.user.mapper;

import com.hean.consigueventas.oonabe.user.dto.UserDTO;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserDTO toDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isActive(),
                user.getDisabledAt(),
                user.getCreatedAt(),
                roles
        );
    }

    public List<UserDTO> toDtoList(List<User> users) {
        return users.stream().map(this::toDto).toList();
    }
}
