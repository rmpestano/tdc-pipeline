package com.github.rmpestano.tdc.smoke;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(JUnit4.class)
public class CarsSmoke {

    private WebTarget target;
    private final String HEAÇTH_CHECK_CONTEXT = "http://localhost:8080/tdc-cars/rest/health";

    @Before
    public void init() {
        target = ClientBuilder.newClient().target(HEAÇTH_CHECK_CONTEXT);
    }

    @Test
    public void shouldCheckCarsDatasource() {
        Response response = target.path("/datasource").request().get();
        assertThat(response).isNotNull()
                .extracting("status")
                .contains(200);
    }

    @Test
    public void shouldCheckCarService() {
        Response response = target.path("/car-service").request().get();
        assertThat(response).isNotNull()
                .extracting("status")
                .contains(200);
    }
}
