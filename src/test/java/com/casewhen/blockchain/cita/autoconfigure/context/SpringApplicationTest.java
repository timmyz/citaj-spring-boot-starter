package com.casewhen.blockchain.cita.autoconfigure.context;

import com.cryptape.cita.protocol.CITAj;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@SpringBootApplication
public class SpringApplicationTest {
    @Bean
    @Primary
    public CITAj nameService() {
        return mock(CITAj.class, Mockito.RETURNS_DEEP_STUBS);
    }

}
