package com.hean.consigueventas.oonabe.location.service;

import com.hean.consigueventas.oonabe.location.dto.response.LocationResponse;
import java.util.List;

public interface ILocationService {
    List<LocationResponse> findActive();
}
