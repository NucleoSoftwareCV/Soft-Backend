package com.hean.consigueventas.oonabe.masterdata.service;

import com.hean.consigueventas.oonabe.masterdata.dto.Admin.CityAdminDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.User.CityPublicDTO;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.mapper.CityMapper;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    public CityService(
            CityRepository cityRepository,
            CityMapper cityMapper
    ) {
        this.cityRepository = cityRepository;
        this.cityMapper = cityMapper;
    }

    // Para usuarios: solo ciudades activas
    @Transactional(readOnly = true)
    public List<CityPublicDTO> findActive() {
        return cityRepository.findByIsActiveTrue()
                .stream()
                .map(cityMapper::toDto)
                .toList();
    }

    // Para administrador: todas o filtradas por estado
    @Transactional(readOnly = true)
    public List<CityAdminDTO> findAllForAdmin(Boolean active) {

        List<City> cities;

        if (active == null) {
            cities = cityRepository.findAll();
        } else {
            cities = cityRepository.findByIsActive(active);
        }

        return cities.stream()
                .map(cityMapper::toAdminDto)
                .toList();
    }
}