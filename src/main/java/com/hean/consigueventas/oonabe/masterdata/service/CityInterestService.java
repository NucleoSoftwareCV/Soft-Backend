package com.hean.consigueventas.oonabe.masterdata.service;

import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.masterdata.dto.request.CityInterestRequest;
import com.hean.consigueventas.oonabe.masterdata.dto.response.CityInterestResponse;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.CityInterest;
import com.hean.consigueventas.oonabe.masterdata.repository.CityInterestRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CityInterestService {

    private final CityInterestRepository cityInterestRepository;
    private final CityRepository cityRepository;

    public CityInterestService(
            CityInterestRepository cityInterestRepository,
            CityRepository cityRepository
    ) {
        this.cityInterestRepository = cityInterestRepository;
        this.cityRepository = cityRepository;
    }

    // Publico: registra el interes de un usuario por una ciudad
    @Transactional
    public CityInterestResponse register(CityInterestRequest request) {
        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ciudad no encontrada con ID: " + request.cityId()));

        if (!Boolean.TRUE.equals(city.getIsActive())) {
            throw new BusinessLogicException(
                    "La ciudad no esta activa: " + city.getName());
        }

        String normalizedEmail = request.email().trim().toLowerCase();

        if (cityInterestRepository.existsByCityIdAndEmailIgnoreCase(city.getId(), normalizedEmail)) {
            throw new BusinessLogicException(
                    "Ya has registrado tu interes por esta ciudad con este email.");
        }

        CityInterest cityInterest = new CityInterest();
        cityInterest.setCity(city);
        cityInterest.setEmail(normalizedEmail);
        cityInterestRepository.save(cityInterest);

        return new CityInterestResponse(
                "Gracias por tu interes. Te avisaremos cuando Oona llegue a tu ciudad.");
    }
}
