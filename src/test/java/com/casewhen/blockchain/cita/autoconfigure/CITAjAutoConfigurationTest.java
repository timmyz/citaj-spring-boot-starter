package com.casewhen.blockchain.cita.autoconfigure;

import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.CITAjService;
import com.cryptape.cita.protocol.Service;
import com.cryptape.cita.protocol.admin.Admin;
import com.cryptape.cita.protocol.core.JsonRpc2_0CITAj;
import com.cryptape.cita.protocol.http.HttpService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CITAjAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void testEmptyClientAddress() throws Exception {
        verifyHttpConnection("", HttpService.DEFAULT_URL, HttpService.class);
    }

    @Test
    public void testHttpClient() throws Exception {
        verifyHttpConnection(
                "http://121.196.200.225:1337", HttpService.class);
    }

    @Test
    public void testUnixIpcClient() throws IOException {
        Path path = Files.createTempFile("unix", "ipc");
        path.toFile().deleteOnExit();

        load(EmptyConfiguration.class, "citaj.client-address=" + path.toString());
    }

    @Test
    public void testWindowsIpcClient() throws IOException {
        // Windows uses a RandomAccessFile to access the named pipe, hence we can initialise
        // the WindowsIPCService in citaj
        Path path = Files.createTempFile("windows", "ipc");
        path.toFile().deleteOnExit();

        System.setProperty("os.name", "windows");
        load(EmptyConfiguration.class, "citaj.client-address=" + path.toString());
    }

    @Test
    public void testAdminClient() {
        load(EmptyConfiguration.class, "citaj.client-address=", "citaj.admin-client=true");

        this.context.getBean(Admin.class);
        try {
            this.context.getBean(CITAj.class);
            fail();
        } catch (NoSuchBeanDefinitionException e) {
        }
    }

    @Test
    public void testNoAdminClient() {
        load(EmptyConfiguration.class, "citaj.client-address=");

        this.context.getBean(CITAj.class);
        try {
            this.context.getBean(Admin.class);
            fail();
        } catch (NoSuchBeanDefinitionException e) {
        }
    }


    @Test
    public void testHealthCheckIndicatorDown() {
        load(EmptyConfiguration.class, "citaj.client-address=");

        HealthIndicator citajHealthIndicator = this.context.getBean(HealthIndicator.class);
        Health health = citajHealthIndicator.health();
        assertThat(health.getStatus(), equalTo(Status.DOWN));
        assertThat(health.getDetails().get("error").toString(),
                containsString("java.net.ConnectException: Failed to connect to localhost/"));
    }

    private void verifyHttpConnection(
            String clientAddress, Class<? extends Service> cls) throws Exception {
        verifyHttpConnection(clientAddress, clientAddress, cls);
    }

    private void verifyHttpConnection(
            String clientAddress, String expectedClientAddress, Class<? extends Service> cls)
            throws Exception {
        load(EmptyConfiguration.class, "citaj.client-address=" + clientAddress);
        CITAj citaj = this.context.getBean(CITAj.class);

        Field citajServiceField = JsonRpc2_0CITAj.class.getDeclaredField("CITAjService");
        citajServiceField.setAccessible(true);
        CITAjService citajService = (CITAjService) citajServiceField.get(citaj);

        assertTrue(cls.isInstance(citajService));

        Field urlField = HttpService.class.getDeclaredField("url");
        urlField.setAccessible(true);
        String url = (String) urlField.get(citajService);

        assertThat(url, equalTo(expectedClientAddress));
    }

    @Configuration
    static class EmptyConfiguration {
    }

    private void load(Class<?> config, String... environment) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        TestPropertyValues.of(environment).applyTo(applicationContext);
        applicationContext.register(config);
        applicationContext.register(CITAjAutoConfiguration.class);
        applicationContext.refresh();
        this.context = applicationContext;
    }

}
