package com.hean.consigueventas.oonabe.catalog.service.impl;

import com.hean.consigueventas.oonabe.catalog.service.ICityService;

import com.hean.consigueventas.oonabe.catalog.dto.response.CityResponse;
import com.hean.consigueventas.oonabe.catalog.mapper.CityMapper;
import com.hean.consigueventas.oonabe.catalog.repository.CityRepository;
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

    @Transactional(readOnly = true)
    @Override
    public List<CityResponse> findActive() {
        return cityRepository.findByIsActiveTrueOrderByNameAsc().stream().map(cityMapper::toDto).toList();
    }

}
