package com.hean.consigueventas.oonabe.profileCliente.dto.response;
import com.hean.consigueventas.oonabe.profileCliente.entity.ReferenceLocation;
public record ReferenceLocationResponse(String label, Double latitude, Double longitude, Boolean approximate) {
    public static ReferenceLocationResponse from(ReferenceLocation value) {
        return value == null ? null : new ReferenceLocationResponse(value.getLabel(), value.getLatitude(), value.getLongitude(), value.getApproximate());
    }
}
