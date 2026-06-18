package com.hean.consigueventas.oonabe.location.service.impl;

import com.hean.consigueventas.oonabe.location.service.ILocationService;

import com.hean.consigueventas.oonabe.location.dto.response.LocationResponse;
import com.hean.consigueventas.oonabe.location.mapper.LocationMapper;
import com.hean.consigueventas.oonabe.location.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationServiceImpl implements ILocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public LocationServiceImpl(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public List<LocationResponse> findActive() {
        return locationRepository.findByIsActiveTrueOrderByNameAsc().stream().map(locationMapper::toDto).toList();
    }






}
