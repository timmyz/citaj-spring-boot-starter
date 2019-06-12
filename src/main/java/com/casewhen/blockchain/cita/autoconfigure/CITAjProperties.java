package com.casewhen.blockchain.cita.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.casewhen.blockchain.cita.autoconfigure.CITAjProperties.CITAJ_PREFIX;

/**
 * citaj property container.
 */
@ConfigurationProperties(prefix = CITAJ_PREFIX)
public class CITAjProperties {

    public static final String CITAJ_PREFIX = "citaj";

    private String clientAddress;

    private Boolean adminClient;

    private Long httpTimeoutSeconds;

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public Boolean isAdminClient() {
        return adminClient;
    }

    public void setAdminClient(Boolean adminClient) {
        this.adminClient = adminClient;
    }

    public Long getHttpTimeoutSeconds() {
        return httpTimeoutSeconds;
    }

    public void setHttpTimeoutSeconds(Long httpTimeoutSeconds) {
        this.httpTimeoutSeconds = httpTimeoutSeconds;
    }
    
}
