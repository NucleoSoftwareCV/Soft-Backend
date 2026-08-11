package com.hean.consigueventas.oonabe.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final int maxAttempts;
    private final long windowMillis;
    private final Set<String> trustedProxies;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(
            @Value("${app.security.auth-rate-limit.max-attempts:30}") int maxAttempts,
            @Value("${app.security.auth-rate-limit.window-ms:900000}") long windowMillis,
            @Value("${app.security.trusted-proxies:}") List<String> trustedProxies) {
        this.maxAttempts = maxAttempts;
        this.windowMillis = windowMillis;
        this.trustedProxies = Set.copyOf(trustedProxies);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!isLimitedAuthEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientKey(request);
        long now = System.currentTimeMillis();
        Bucket bucket = buckets.compute(key, (ignored, current) -> {
            if (current == null || now - current.windowStartedAt >= windowMillis) {
                return new Bucket(now, 1);
            }
            return new Bucket(current.windowStartedAt, current.attempts + 1);
        });

        if (bucket.attempts > maxAttempts) {
            response.setStatus(429);
            response.setContentType("application/problem+json");
            response.getWriter().write("""
                    {"type":"https://api.oona.local/errors/rate-limit","title":"Demasiadas solicitudes","status":429,"detail":"Demasiados intentos de autenticación. Intenta nuevamente mas tarde.","message":"Demasiados intentos de autenticación. Intenta nuevamente mas tarde."}
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLimitedAuthEndpoint(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh-token")
                || path.equals("/api/auth/forgot-password")
                || path.equals("/api/auth/reset-password")
                || path.equals("/api/v1/auth/admin/login");
    }

    private String clientKey(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String forwardedFor = request.getHeader("X-Forwarded-For");

        // Solo confiamos en X-Forwarded-For si la conexion inmediata viene de un
        // proxy conocido; de lo contrario un cliente podria falsificarlo para
        // esquivar el limite de intentos.
        String ip = (trustedProxies.contains(remoteAddr) && forwardedFor != null && !forwardedFor.isBlank())
                ? forwardedFor.split(",")[0].trim()
                : remoteAddr;
        return ip + ":" + request.getRequestURI();
    }

    private record Bucket(long windowStartedAt, int attempts) {
    }
}
