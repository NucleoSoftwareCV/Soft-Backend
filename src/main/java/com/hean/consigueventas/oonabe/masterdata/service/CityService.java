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

    // Admin: crear una ciudad
    @Transactional
    public CityAdminDTO createCity(CityAdminDTO cityAdminDTO) {
        String name = cityAdminDTO.name().trim();
        String province = cityAdminDTO.province() != null ? cityAdminDTO.province().trim() : null;

        // Validar duplicado por nombre y provincia
        if (cityRepository.findByNameAndProvince(name, province).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe una ciudad con el nombre: " + name +
                    (province != null ? " en la provincia: " + province : "")
            );
        }

        City city = cityMapper.toEntity(cityAdminDTO);
        city.setName(name);
        city.setProvince(province);
        city.setIsActive(true); // por defecto activa al crear

        City saved = cityRepository.save(city);
        return cityMapper.toAdminDto(saved);
    }

    // Admin: actualizar una ciudad
    @Transactional
    public CityAdminDTO updateCity(Long id, CityAdminDTO cityAdminDTO) {
        City existingCity = cityRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Ciudad no encontrada con ID: " + id)
                );

        String name = cityAdminDTO.name().trim();
        String province = cityAdminDTO.province() != null ? cityAdminDTO.province().trim() : null;

        // Validar duplicado excluyéndose a sí misma
        cityRepository.findByNameAndProvince(name, province).ifPresent(c -> {
            if (!c.getId().equals(id)) {
                throw new IllegalArgumentException(
                        "Ya existe otra ciudad con el nombre: " + name +
                        (province != null ? " en la provincia: " + province : "")
                );
            }
        });

        cityMapper.updateEntityFromDto(cityAdminDTO, existingCity);
        existingCity.setName(name);
        existingCity.setProvince(province);

        City updated = cityRepository.save(existingCity);
        return cityMapper.toAdminDto(updated);
    }

    // Admin: eliminar físicamente una ciudad
    @Transactional
    public void deleteCity(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Ciudad no encontrada con ID: " + id)
                );
        cityRepository.delete(city);
    }
}
