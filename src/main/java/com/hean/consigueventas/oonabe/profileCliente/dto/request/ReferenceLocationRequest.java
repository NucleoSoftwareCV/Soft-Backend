package com.hean.consigueventas.oonabe.profileCliente.dto.request;
import jakarta.validation.constraints.*;
public record ReferenceLocationRequest(
    @NotBlank @Size(max=300) String label,
    @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
    @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
    @NotNull Boolean approximate
) {}
