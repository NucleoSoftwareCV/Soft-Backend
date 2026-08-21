package com.hean.consigueventas.oonabe.masterdata.dto.User;

public record CityPublicDTO(
        Long id,
        String name,
        String province,
        String countryCode,
        boolean active
) {
}