package com.hean.consigueventas.oonabe.contact;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactEmailRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        String email,

        boolean profesional

) {
}