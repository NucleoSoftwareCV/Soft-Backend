package com.hean.consigueventas.oonabe.dto.user;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class AdminUserUpdateDTO {
    private String username;
    @Email(message = "Formato de correo inválido")
    private String email;
    private String newPassword;
    private Boolean active;
    private Set<String> roles; // Ej: ["ADMIN", "USER"]
}