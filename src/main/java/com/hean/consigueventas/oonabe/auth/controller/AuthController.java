package com.hean.consigueventas.oonabe.auth.controller;

import com.hean.consigueventas.oonabe.auth.dto.GoogleLoginRequest;
import com.hean.consigueventas.oonabe.auth.dto.JwtResponse;
import com.hean.consigueventas.oonabe.auth.dto.LoginRequest;
import com.hean.consigueventas.oonabe.auth.dto.RegisterRequest;
import com.hean.consigueventas.oonabe.auth.dto.TokenRefreshRequest;
import com.hean.consigueventas.oonabe.auth.dto.TokenRefreshResponse;
import com.hean.consigueventas.oonabe.auth.service.AuthService;
import com.hean.consigueventas.oonabe.user.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Registro, login, refresh token y cierre de sesion.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuario", description = "Crea un usuario final con rol USER.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o usuario duplicado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public UserDTO register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Autentica con username o email y devuelve access token y refresh token.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesion iniciada",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public JwtResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.loginWithGoogle(request);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Renovar access token", description = "Genera un nuevo access token usando un refresh token vigente.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado",
                    content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class))),
            @ApiResponse(responseCode = "403", description = "Refresh token invalido o expirado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public TokenRefreshResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cerrar sesion", description = "Revoca el refresh token recibido.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sesion cerrada"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void logout(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request);
    }
}
