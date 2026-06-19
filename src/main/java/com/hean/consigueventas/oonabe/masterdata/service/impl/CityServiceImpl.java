package com.hean.consigueventas.oonabe.masterdata.service.impl;

import com.hean.consigueventas.oonabe.catalog.dto.response.CityResponse;
import com.hean.consigueventas.oonabe.masterdata.mapper.CityMapper;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;

import com.hean.consigueventas.oonabe.masterdata.service.ICityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityServiceImpl implements ICityService {
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    public CityServiceImpl(CityRepository cityRepository, CityMapper cityMapper) {
        this.cityRepository = cityRepository;
        this.cityMapper = cityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityResponse> findActive() {
        return cityRepository.findByIsActiveTrueOrderByNameAsc().stream().map(cityMapper::toDto).toList();
    }

}
