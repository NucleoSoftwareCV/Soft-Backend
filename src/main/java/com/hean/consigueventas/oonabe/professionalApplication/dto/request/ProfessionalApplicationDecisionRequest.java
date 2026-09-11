package com.hean.consigueventas.oonabe.professionalApplication.dto.request;

import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Decisión administrativa sobre una solicitud profesional")
public record ProfessionalApplicationDecisionRequest(

        @NotNull(message = "La decisión es obligatoria")
        ProfessionalApplicationStatus status,

        @Size(
                max = 500,
                message = "El motivo no puede superar los 500 caracteres"
        )
        String rejectionReason

) {

    @AssertTrue(
            message = "La decisión debe ser APROBADO o RECHAZADO y todo rechazo requiere un motivo"
    )
    public boolean isValidDecision() {

        if (status == null ||
                status == ProfessionalApplicationStatus.PENDIENTE) {

            return false;
        }

        return status !=
                ProfessionalApplicationStatus.RECHAZADO
                || (rejectionReason != null
                && !rejectionReason.isBlank());
    }
}