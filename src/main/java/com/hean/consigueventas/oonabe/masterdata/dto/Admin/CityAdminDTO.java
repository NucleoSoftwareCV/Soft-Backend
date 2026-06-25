package com.hean.consigueventas.oonabe.masterdata.dto.Admin;

public record CityAdminDTO (
        Long id,
        String name,
        String province,
        String countryCode,
        boolean active
) {
}
