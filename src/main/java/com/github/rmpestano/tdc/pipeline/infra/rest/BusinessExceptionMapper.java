package com.github.rmpestano.tdc.pipeline.infra.rest;

import com.github.adminfaces.template.exception.BusinessException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;

import static com.github.adminfaces.template.util.Assert.has;

/**
 * Created by rmpestano on 12/20/14.
 */
@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    @Override
    public Response toResponse(BusinessException e) {
        List<BusinessException> exceptions = new ArrayList<>();
        if(has(e.getExceptionList())) {
            for (BusinessException businessException : e.getExceptionList()) {
                exceptions.add(businessException);
            }
        } else {
            exceptions.add(e);
        }
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(exceptions).build();
    }
}
