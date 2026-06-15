package com.hean.consigueventas.oonabe.common.security;

public final class SecurityConstants {
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String PROFESSIONAL = "PROFESSIONAL";

    public static final String HAS_ROLE_ADMIN = "hasRole('" + ADMIN + "')";
    public static final String HAS_ROLE_USER = "hasRole('" + USER + "')";
    public static final String HAS_ROLE_PROFESSIONAL = "hasRole('" + PROFESSIONAL + "')";

    private SecurityConstants() {
    }
}
