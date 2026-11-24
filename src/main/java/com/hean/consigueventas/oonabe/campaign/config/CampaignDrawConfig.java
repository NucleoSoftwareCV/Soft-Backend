package com.hean.consigueventas.oonabe.campaign.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

/** Fuente aleatoria del sorteo; separada en un bean para poder fijarla en las pruebas. */
@Configuration
public class CampaignDrawConfig {

    @Bean
    public RandomGenerator campaignDrawRandom() {
        return new SecureRandom();
    }
}
