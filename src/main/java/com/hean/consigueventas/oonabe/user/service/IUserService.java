package com.hean.consigueventas.oonabe.user.service;

import com.hean.consigueventas.oonabe.user.dto.response.UserResponse;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;

public interface IUserService {
    User registerUser(User user);
    Role getOrCreateRole(String name, String description);
    UserResponse findById(Long id);
    User findEntityById(Long id);
}
