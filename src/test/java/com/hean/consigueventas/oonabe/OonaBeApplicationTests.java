package com.hean.consigueventas.oonabe;

import com.hean.consigueventas.oonabe.auth.service.AuthService;
import com.hean.consigueventas.oonabe.auth.service.RefreshTokenService;
import com.hean.consigueventas.oonabe.category.service.CategoryService;
import com.hean.consigueventas.oonabe.event.service.EventOccurrenceService;
import com.hean.consigueventas.oonabe.masterdata.service.CityService;
import com.hean.consigueventas.oonabe.masterdata.service.LocationService;
import com.hean.consigueventas.oonabe.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OonaBeApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
    }

    @Test
    void expectedServicesAreRegisteredOnce() {
        assertSingleBean(AuthService.class);
        assertSingleBean(RefreshTokenService.class);
        assertSingleBean(CityService.class);
        assertSingleBean(CategoryService.class);
        assertSingleBean(EventOccurrenceService.class);
        assertSingleBean(LocationService.class);
        assertSingleBean(UserService.class);
    }

    private void assertSingleBean(Class<?> serviceType) {
        org.assertj.core.api.Assertions.assertThat(applicationContext.getBeansOfType(serviceType)).hasSize(1);
    }

}

