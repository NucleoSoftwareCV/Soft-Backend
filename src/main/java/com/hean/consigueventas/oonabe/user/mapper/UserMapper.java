package com.hean.consigueventas.oonabe.user.mapper;

import com.hean.consigueventas.oonabe.user.dto.response.UserResponse;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toDto(User user);

    List<UserResponse> toDtoList(List<User> users);

    default Set<String> mapRoles(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toSet());
    }
}
