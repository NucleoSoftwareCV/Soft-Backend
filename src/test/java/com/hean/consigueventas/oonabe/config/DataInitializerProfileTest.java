package com.hean.consigueventas.oonabe.config;

import com.hean.consigueventas.oonabe.common.config.DataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import static org.assertj.core.api.Assertions.assertThat;

class DataInitializerProfileTest {

    @Test
    void dataInitializerRunsForLocalDevelopmentProfiles() {
        Profile profile = DataInitializer.class.getAnnotation(Profile.class);

        assertThat(profile).isNotNull();
        assertThat(profile.value()).containsExactlyInAnyOrder("default", "dev", "local", "test");
    }
}
