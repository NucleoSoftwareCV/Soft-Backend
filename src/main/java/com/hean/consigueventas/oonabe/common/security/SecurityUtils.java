package com.hean.consigueventas.oonabe.common.security;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityUtils")
public class SecurityUtils {

    public static Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return ((UserDetailsImpl) auth.getPrincipal()).getId();
    }

    public boolean isOwner(Long referenceId) {
        Long authenticatedId = getAuthenticatedUserId();
        return authenticatedId != null && authenticatedId.equals(referenceId);
    }
}
