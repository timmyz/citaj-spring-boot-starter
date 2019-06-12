package com.casewhen.blockchain.cita.actuate;

import com.cryptape.cita.protocol.CITAj;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.util.Assert;

/**
 * Health check indicator for citaj
 */
public class CITAjHealthIndicator extends AbstractHealthIndicator {

    private CITAj citaj;

    public CITAjHealthIndicator(CITAj citaj) {
        Assert.notNull(citaj, "citaj must not be null");
        this.citaj = citaj;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            builder.up();
            builder.withDetail("blockNumber", citaj.appBlockNumber().send().getBlockNumber());
        } catch (Exception ex) {
            builder.down(ex);
        }
    }
}
