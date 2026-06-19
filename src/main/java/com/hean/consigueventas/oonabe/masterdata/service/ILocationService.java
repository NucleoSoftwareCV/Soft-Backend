package com.hean.consigueventas.oonabe.masterdata.service;

import com.hean.consigueventas.oonabe.location.dto.response.LocationResponse;
import java.util.List;

public interface ILocationService {
    List<LocationResponse> findActive();
}