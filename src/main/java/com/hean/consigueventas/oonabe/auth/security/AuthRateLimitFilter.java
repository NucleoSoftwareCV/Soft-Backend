package com.hean.consigueventas.oonabe.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final int maxAttempts;
    private final long windowMillis;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(
            @Value("${app.security.auth-rate-limit.max-attempts:30}") int maxAttempts,
            @Value("${app.security.auth-rate-limit.window-ms:900000}") long windowMillis) {
        this.maxAttempts = maxAttempts;
        this.windowMillis = windowMillis;
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
                    {"type":"https://api.oona.local/errors/rate-limit","title":"Demasiadas solicitudes","status":429,"detail":"Demasiados intentos de autenticacion. Intenta nuevamente mas tarde.","message":"Demasiados intentos de autenticacion. Intenta nuevamente mas tarde."}
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
        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/refresh-token");
    }

    private String clientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ip = forwardedFor == null || forwardedFor.isBlank()
                ? request.getRemoteAddr()
                : forwardedFor.split(",")[0].trim();
        return ip + ":" + request.getRequestURI();
    }

    private record Bucket(long windowStartedAt, int attempts) {
    }
}
