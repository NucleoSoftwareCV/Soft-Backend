package com.hean.consigueventas.oonabe.auth.service;

import com.hean.consigueventas.oonabe.auth.dto.request.GoogleLoginRequest;
import com.hean.consigueventas.oonabe.auth.dto.response.JwtResponse;
import com.hean.consigueventas.oonabe.auth.dto.request.LoginRequest;
import com.hean.consigueventas.oonabe.auth.dto.request.RegisterRequest;
import com.hean.consigueventas.oonabe.auth.dto.request.TokenRefreshRequest;
import com.hean.consigueventas.oonabe.auth.dto.response.TokenRefreshResponse;
import com.hean.consigueventas.oonabe.auth.entity.RefreshToken;
import com.hean.consigueventas.oonabe.auth.security.GoogleTokenVerifier;
import com.hean.consigueventas.oonabe.auth.security.JwtUtils;
import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.common.exception.TokenRefreshException;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientProfileRepository;
import com.hean.consigueventas.oonabe.user.dto.response.UserResponse;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.mapper.UserMapper;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import com.hean.consigueventas.oonabe.user.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final ClientProfileRepository clientProfileRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                       UserService userService, UserMapper userMapper,
                       RefreshTokenService refreshTokenService, UserRepository userRepository,
                       GoogleTokenVerifier googleTokenVerifier, ClientProfileRepository clientProfileRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.userMapper = userMapper;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.googleTokenVerifier = googleTokenVerifier;
        this.clientProfileRepository = clientProfileRepository;
    }

    private String firstNameFor(Long userId) {
        return clientProfileRepository.findByUserId(userId)
                .map(profile -> profile.getFirstName())
                .orElse(null);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        return userMapper.toDto(userService.registerUser(user, request.firstName(), request.lastName()));
    }

    @Transactional
    public JwtResponse login(LoginRequest request) {
        return authenticate(request, false);
    }

    @Transactional
    public JwtResponse adminLogin(LoginRequest request) {
        return authenticate(request, true);
    }

    private JwtResponse authenticate(LoginRequest request, boolean adminLogin) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (adminLogin != isAdmin) {
            throw new AccessDeniedException("Acceso no permitido para este portal.");
        }

        String jwt = jwtUtils.generateJwtToken(authentication);
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), firstNameFor(userDetails.getId()), roles);
    }

    @Transactional
    public JwtResponse loginWithGoogle(GoogleLoginRequest request) {
        Map<String, Object> response = googleTokenVerifier.verifyToken(request.idToken());

        String email = (String) response.get("email");
        if (email == null) {
            throw new BadCredentialsException("No se pudo obtener el email del token de Google.");
        }

        String givenName = (String) response.getOrDefault("given_name", "");
        String familyName = (String) response.getOrDefault("family_name", "");

        User user = userRepository.findByUsernameOrEmail(email, email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(generateUniqueUsername(email));
                    newUser.setPassword(UUID.randomUUID().toString());
                    newUser.setActive(true);
                    return userService.registerUser(newUser, givenName, familyName);
                });

        if (!user.isActive()) {
            throw new BadCredentialsException("El usuario esta inactivo.");
        }
        if (user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()))) {
            throw new AccessDeniedException("Acceso no permitido para este portal.");
        }

        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return new JwtResponse(jwt, refreshToken.getToken(), user.getId(), user.getUsername(), user.getEmail(),
                firstNameFor(user.getId()), roles);
    }

    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        if (base.length() > 15) {
            base = base.substring(0, 15);
        }
        if (base.isEmpty()) {
            base = "user";
        }

        String username = base;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            String suffix = String.valueOf(counter);
            if (base.length() + suffix.length() > 20) {
                base = base.substring(0, 20 - suffix.length());
            }
            username = base + suffix;
            counter++;
        }
        return username;
    }

    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    Set<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().replace("ROLE_", ""))
                            .collect(Collectors.toSet());
                    // Rotamos el refresh token en cada uso: el anterior queda invalidado de inmediato.
                    RefreshToken rotated = refreshTokenService.createRefreshToken(user.getId());
                    return new TokenRefreshResponse(token, rotated.getToken(), roles);
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "El refresh token no existe."));
    }

    @Transactional
    public void logout(TokenRefreshRequest request) {
        refreshTokenService.deleteByToken(request.refreshToken());
    }
}
