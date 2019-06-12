package com.casewhen.blockchain.cita.autoconfigure;

import com.casewhen.blockchain.cita.autoconfigure.context.SpringApplicationTest;
import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.methods.response.AppBlockNumber;
import com.cryptape.cita.protocol.core.methods.response.AppVersion;
import com.cryptape.cita.protocol.core.methods.response.AppVersion.Version;
import com.cryptape.cita.protocol.core.methods.response.NetPeerCount;
import com.cryptape.cita.utils.Numeric;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringApplicationTest.class)
public class CITAjHealthIndicatorTest {


    @Autowired
    HealthIndicator citajHealthIndicator;

    @Autowired
    CITAj citaj;

    //@Test
    public void testHealthCheckIndicatorDown() throws Exception {
        mockCITAjCalls(null, null, null);
        Health health = citajHealthIndicator.health();
        assertThat(health.getStatus(), equalTo(Status.DOWN));

    }

    @Test
    public void testHealthCheckIndicatorUp() throws Exception {

        mockCITAjCalls("23",
            new BigInteger("120"), new BigInteger("80"));

        Health health = citajHealthIndicator.health();
        assertThat(health.getStatus(), equalTo(Status.UP));
        assertThat(health.getDetails().get("blockNumber"), equalTo(new BigInteger("120")));

    }

    private void mockCITAjCalls(String netVersion,
        BigInteger blockNumber, BigInteger netPeer) throws Exception {

        if (blockNumber != null) {
            AppBlockNumber appBlockNumber = new AppBlockNumber();
            appBlockNumber.setResult(Numeric.encodeQuantity(blockNumber));
            Mockito.when(citaj.appBlockNumber().send()).thenReturn(appBlockNumber);
        }
    }

}