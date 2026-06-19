package com.hean.consigueventas.oonabe.masterdata.service;

import com.hean.consigueventas.oonabe.masterdata.dto.LocationDTO;
import com.hean.consigueventas.oonabe.masterdata.mapper.LocationMapper;
import com.hean.consigueventas.oonabe.masterdata.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public LocationService(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    @Transactional(readOnly = true)
    public List<LocationDTO> findActive() {
        return locationRepository.findByIsActiveTrueOrderByNameAsc().stream().map(locationMapper::toDto).toList();
    }






}
