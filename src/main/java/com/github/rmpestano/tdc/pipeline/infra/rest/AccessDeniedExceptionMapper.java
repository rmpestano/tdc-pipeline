package com.github.rmpestano.tdc.pipeline.infra.rest;

import com.github.adminfaces.template.exception.AccessDeniedException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmpestano on 12/20/14.
 */
@Provider
public class AccessDeniedExceptionMapper implements ExceptionMapper<AccessDeniedException> {

    @Override
    public Response toResponse(AccessDeniedException e) {
        List<AccessDeniedException> exceptions = new ArrayList<>();
        exceptions.add(e);
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(exceptions).build();
    }
}
