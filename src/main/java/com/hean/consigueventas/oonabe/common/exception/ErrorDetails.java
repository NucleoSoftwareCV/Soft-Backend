package com.hean.consigueventas.oonabe.common.exception;

import java.time.LocalDateTime;

public record ErrorDetails(LocalDateTime timestamp, String message, String details) {
}
