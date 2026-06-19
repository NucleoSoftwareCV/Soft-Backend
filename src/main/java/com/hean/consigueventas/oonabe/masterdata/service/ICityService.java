package com.hean.consigueventas.oonabe.masterdata.service;

import com.hean.consigueventas.oonabe.masterdata.response.CityResponse;

import java.util.List;

public interface ICityService {
    List<CityResponse> findActive();
}