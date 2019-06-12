package com.casewhen.blockchain.cita.autoconfigure;

import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.CITAjService;
import com.cryptape.cita.protocol.admin.Admin;
import com.cryptape.cita.protocol.http.HttpService;
import com.cryptape.cita.protocol.ipc.UnixIpcService;
import com.cryptape.cita.protocol.ipc.WindowsIpcService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import com.casewhen.blockchain.cita.actuate.CITAjHealthIndicator;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * citaj auto configuration for Spring Boot.
 */
@Configuration
@ConditionalOnClass(CITAj.class)
@EnableConfigurationProperties(CITAjProperties.class)
public class CITAjAutoConfiguration {

    private static Log log = LogFactory.getLog(CITAjAutoConfiguration.class);

    @Autowired
    private CITAjProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public CITAj citaj() {
        CITAjService citajService = buildService(properties.getClientAddress());
        log.info("Building service for endpoint: " + properties.getClientAddress());
        return CITAj.build(citajService);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = CITAjProperties.CITAJ_PREFIX, name = "admin-client", havingValue = "true")
    public Admin admin() {
        CITAjService citajService = buildService(properties.getClientAddress());
        log.info("Building admin service for endpoint: " + properties.getClientAddress());
        return Admin.build(citajService);
    }

    private CITAjService buildService(String clientAddress) {
        CITAjService citajService;

        if (clientAddress == null || clientAddress.equals("")) {
            citajService = new HttpService(createOkHttpClient());
        } else if (clientAddress.startsWith("http")) {
            citajService = new HttpService(clientAddress, createOkHttpClient(), false);
        } else if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            citajService = new WindowsIpcService(clientAddress);
        } else {
            citajService = new UnixIpcService(clientAddress);
        }

        return citajService;
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        configureLogging(builder);
        configureTimeouts(builder);
        return builder.build();
    }

    private void configureTimeouts(OkHttpClient.Builder builder) {
        Long tos = properties.getHttpTimeoutSeconds();
        if (tos != null) {
            builder.connectTimeout(tos, TimeUnit.SECONDS);
            builder.readTimeout(tos, TimeUnit.SECONDS);  // Sets the socket timeout too
            builder.writeTimeout(tos, TimeUnit.SECONDS);
        }
    }

    private static void configureLogging(OkHttpClient.Builder builder) {
        if (log.isDebugEnabled()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(log::debug);
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
    }


    @Bean
    @ConditionalOnBean(CITAj.class)
    CITAjHealthIndicator citajHealthIndicator(CITAj citaj) {
        return new CITAjHealthIndicator(citaj);
    }
}
