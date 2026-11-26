package com.hean.consigueventas.oonabe.support.service;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupportAccess {
    private final UserRepository users;

    public User user() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof UserDetailsImpl p))
            throw new AccessDeniedException("Inicia sesión");
        var u =
                users
                        .findById(p.getId())
                        .orElseThrow(() -> new AccessDeniedException("Usuario inexistente"));
        if (!u.isActive()) throw new AccessDeniedException("Cuenta deshabilitada");
        return u;
    }

    public boolean isAdmin() {
        return user().getRoles().stream()
                .anyMatch(r -> r.isActive() && r.getName().equals("ROLE_ADMIN"));
    }

    public void admin() {
        if (!isAdmin()) throw new AccessDeniedException("Solo administrador general");
    }

    public boolean canReadAll() {
        return isAdmin();
    }

    public void customer() {
        if (user().getRoles().stream().noneMatch(r -> r.isActive() && r.getName().equals("ROLE_USER"))
                || isAdmin()) throw new AccessDeniedException("Se requiere una cuenta de usuario");
    }
}
