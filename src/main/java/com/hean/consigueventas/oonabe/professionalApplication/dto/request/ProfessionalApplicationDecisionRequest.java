package com.hean.consigueventas.oonabe.professionalApplication.dto.request;

import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Decision administrativa sobre una solicitud profesional")
public record ProfessionalApplicationDecisionRequest(
        @NotNull
        ProfessionalApplicationStatus status,

        @Size(max = 500)
        String rejectionReason
) {
    @AssertTrue(message = "La decision debe ser APROBADO o RECHAZADO y todo rechazo requiere un motivo")
    public boolean isValidDecision() {
        if (status == null || status == ProfessionalApplicationStatus.PENDIENTE) {
            return false;
        }
        return status != ProfessionalApplicationStatus.RECHAZADO
                || (rejectionReason != null && !rejectionReason.isBlank());
    }
}
