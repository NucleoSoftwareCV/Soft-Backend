package com.hean.consigueventas.oonabe.mapper;

import com.hean.consigueventas.oonabe.dto.user.UserDTO;
import com.hean.consigueventas.oonabe.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(r -> r.getNombre().replace(\"ROLE_\", \"\")).collect(java.util.stream.Collectors.toSet()))")
    UserDTO toDto(User user);

    List<UserDTO> toDtoList(List<User> users);
}