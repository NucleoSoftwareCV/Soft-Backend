package com.hean.consigueventas.oonabe.masterdata.service;

import com.hean.consigueventas.oonabe.masterdata.dto.response.LocationResponse;
import com.hean.consigueventas.oonabe.masterdata.mapper.LocationMapper;
import com.hean.consigueventas.oonabe.masterdata.repository.LocationRepository;
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
        return locationRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(locationMapper::toDto)
                .toList();
    }






}
