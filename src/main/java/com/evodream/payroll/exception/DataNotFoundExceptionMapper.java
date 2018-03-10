package com.evodream.payroll.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.evodream.payroll.model.ErrorMessage;

@Provider
public class DataNotFoundExceptionMapper implements ExceptionMapper<DataNotFoundException>{

	@Override
	public Response toResponse(DataNotFoundException ex) {
		ErrorMessage error = new ErrorMessage(ex.getMessage(), 404);
		return Response.status(Status.NOT_FOUND).entity(error).build();
	}	

}
