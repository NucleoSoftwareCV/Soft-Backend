package com.hean.consigueventas.oonabe.persistence;

import com.hean.consigueventas.oonabe.common.security.SensitiveStringCryptoConverter;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.payment.entity.CustomerPaymentMethod;
import jakarta.persistence.Convert;
import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InfrastructureHardeningTest {

//    @Test
//    void eventOccurrencesUseOptimisticLockingForSeatCounters() throws Exception {
//        assertThat(EventOccurrence.class.getDeclaredField("version").getAnnotation(Version.class)).isNotNull();
//    }

    @Test
    void paymentProviderTokensUseJpaEncryptionConverter() throws Exception {
        Convert convert = CustomerPaymentMethod.class.getDeclaredField("token").getAnnotation(Convert.class);

        assertThat(convert).isNotNull();
        assertThat(convert.converter()).isEqualTo(SensitiveStringCryptoConverter.class);
    }
}
