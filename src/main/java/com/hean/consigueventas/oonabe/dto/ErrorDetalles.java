package com.hean.consigueventas.oonabe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ErrorDetalles {
    private LocalDateTime timestamp;
    private String mensaje;
    private String detalles;
}
