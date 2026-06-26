package com.hean.consigueventas.oonabe;

import com.hean.consigueventas.oonabe.auth.service.IAuthService;
import com.hean.consigueventas.oonabe.auth.service.IRefreshTokenService;
import com.hean.consigueventas.oonabe.category.service.ICategoryService;
import com.hean.consigueventas.oonabe.event.service.IEventOccurrenceService;
import com.hean.consigueventas.oonabe.masterdata.service.CityService;
import com.hean.consigueventas.oonabe.masterdata.service.ILocationService;
import com.hean.consigueventas.oonabe.user.service.IUserService;
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
    void eachServiceInterfaceHasExactlyOneImplementation() {
        assertSingleBean(IAuthService.class);
        assertSingleBean(IRefreshTokenService.class);
        assertSingleBean(CityService.class);
        assertSingleBean(ICategoryService.class);
        assertSingleBean(IEventOccurrenceService.class);
        assertSingleBean(ILocationService.class);
        assertSingleBean(IUserService.class);
    }

    private void assertSingleBean(Class<?> serviceType) {
        org.assertj.core.api.Assertions.assertThat(applicationContext.getBeansOfType(serviceType)).hasSize(1);
    }

}

