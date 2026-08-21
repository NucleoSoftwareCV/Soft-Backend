package com.hean.consigueventas.oonabe.auth.controller;

import com.hean.consigueventas.oonabe.auth.dto.request.ForgotPasswordRequest;
import com.hean.consigueventas.oonabe.auth.dto.request.GoogleLoginRequest;
import com.hean.consigueventas.oonabe.auth.dto.request.ResetPasswordRequest;
import com.hean.consigueventas.oonabe.auth.dto.response.JwtResponse;
import com.hean.consigueventas.oonabe.auth.dto.request.LoginRequest;
import com.hean.consigueventas.oonabe.auth.dto.request.RegisterRequest;
import com.hean.consigueventas.oonabe.auth.dto.request.TokenRefreshRequest;
import com.hean.consigueventas.oonabe.auth.dto.response.PasswordResetResponse;
import com.hean.consigueventas.oonabe.auth.dto.response.TokenRefreshResponse;
import com.hean.consigueventas.oonabe.auth.service.AuthService;
import com.hean.consigueventas.oonabe.auth.service.PasswordResetService;
import com.hean.consigueventas.oonabe.user.dto.response.UserResponse;
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
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, login, refresh token y cierre de sesión.")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuario", description = "Crea un usuario final con rol USER.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario duplicado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica con username o email y devuelve access token y refresh token.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión iniciada",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    @Operation(summary = "Iniciar sesión con Google", description = "Valida el token de Google y devuelve los tokens de acceso.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión iniciada",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token de Google inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public JwtResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.loginWithGoogle(request);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Renovar access token", description = "Genera un nuevo access token usando un refresh token vigente.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado",
                    content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class))),
            @ApiResponse(responseCode = "403", description = "Refresh token inválido o expirado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public TokenRefreshResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token recibido.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sesión cerrada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void logout(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request);
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Solicitar recuperación de contraseña",
            description = "Si el email corresponde a una cuenta, envía un enlace de recuperación. Siempre responde igual, exista o no la cuenta.",
            security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud procesada",
                    content = @Content(schema = @Schema(implementation = PasswordResetResponse.class)))
    })
    public PasswordResetResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return new PasswordResetResponse("Si el email existe, te enviamos un enlace para restablecer tu contraseña.");
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Restablecer contraseña",
            description = "Establece una nueva contraseña usando el token recibido por email.",
            security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada",
                    content = @Content(schema = @Schema(implementation = PasswordResetResponse.class))),
            @ApiResponse(responseCode = "400", description = "Token inválido, expirado o ya usado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public PasswordResetResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return new PasswordResetResponse("Tu contraseña se actualizó correctamente. Ya puedes iniciar sesión.");
    }
}
