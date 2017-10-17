package com.github.rmpestano.tdc.cars.rest;


import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;

@Path("health")
public class HealthCheck {

    @Resource(lookup = "java:jboss/datasources/ExampleDS")
    DataSource dataSource;

    @Inject
    HttpServletRequest request;


    @GET
    @Path("datasource")
    public Response checkDataSource() throws SQLException {
        Connection connection = dataSource.getConnection();
        boolean valid = connection.isValid(10);
        if(valid) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("car-service")
    public Response checkCarService() throws SQLException {
        String scheme = request.getScheme();
        String address = request.getServerName();
        int port = request.getLocalPort();
        String context = request.getContextPath();
        String uri = new StringBuilder(scheme).append("://")
                .append(address).append(":")
                .append(port).append(context).toString();

        Response.Status status = Response.Status.fromStatusCode(ClientBuilder.newClient().target(uri)
                .path("/rest/cars")
                .request().get().getStatus());
        return Response.status(status).build();
    }


}
