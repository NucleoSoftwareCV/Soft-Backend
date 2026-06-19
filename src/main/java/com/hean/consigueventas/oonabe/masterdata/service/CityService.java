package com.hean.consigueventas.oonabe.masterdata.service;

import com.hean.consigueventas.oonabe.masterdata.dto.CityDTO;
import com.hean.consigueventas.oonabe.masterdata.mapper.CityMapper;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    public  CityService(CityRepository cityRepository, CityMapper cityMapper) {
        this.cityRepository = cityRepository;
        this.cityMapper = cityMapper;
    }

    @Transactional(readOnly = true)
    public List<CityDTO> findActive() {
        return cityRepository.findByIsActiveTrueOrderByNameAsc().stream().map(cityMapper::toDto).toList();
    }

}
