/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institute of High Frequency Technology
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.rest.resources;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.NamedObject;
import org.dapnet.core.rest.JsonConverter;
import org.dapnet.core.rest.RestAuthorizable;
import org.dapnet.core.rest.RestListener;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;
import org.dapnet.core.rest.exceptionHandling.NoQuorumException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

public abstract class AbstractResource {
	@Context
	protected UriInfo uriInfo;
	@Context
	protected HttpHeaders httpHeaders;

	@Inject
	private CoreRepository repository;
	@Inject
	private RestSecurity restSecurity;
	@Inject
	private RestListener restListener;
	@Inject
	private JsonConverter jsonConverter;

	protected CoreRepository getRepository() {
		return repository;
	}

	protected RestListener getRestListener() {
		return restListener;
	}

	protected JsonConverter getJsonConverter() {
		return jsonConverter;
	}

	// Authorization Helper
	protected RestSecurity.SecurityStatus checkAuthorization(RestSecurity.SecurityLevel level,
			RestAuthorizable restAuthorizable) throws Exception {
		RestSecurity.SecurityStatus status = restSecurity.getStatus(httpHeaders, level, restAuthorizable);

		switch (status) {
		case INTERNAL_ERROR:
			throw new InternalServerErrorException();
		case UNAUTHORIZED:
			throw new NotAuthorizedException(Response.status(Response.Status.UNAUTHORIZED).build());
		case FORBIDDEN:
			throw new ForbiddenException();
		default:
			return status;
		}
	}

	protected RestSecurity.SecurityStatus checkAuthorization(RestSecurity.SecurityLevel level) throws Exception {
		return checkAuthorization(level, null);
	}

	protected <T> void validateObject(T object) {
		Set<ConstraintViolation<T>> constraintViolations = repository.validate(object);
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}

	// Operation Helper
	protected Response getObject(Object object, RestSecurity.SecurityStatus status) throws Exception {
		if (object == null) {
			throw new NotFoundException();
		}

		return Response.ok(jsonConverter.toJson(object, status)).build();
	}

	public Response handleObject(Object object, String methodName, boolean creation, boolean quorumNeeded)
			throws Exception {
		// Check Quorum
		if (quorumNeeded && !restListener.isQuorum()) {
			throw new NoQuorumException();
		}

		// Validation
		if (object == null) {
			throw new EmptyBodyException();
		}

		validateObject(object);

		// Send to Cluster
		if (restListener.handleStateOperation(null, methodName, new Object[] { object },
				new Class[] { object.getClass() })) {
			final String json = jsonConverter.toJson(object);
			if (creation) {
				return Response.created(uriInfo.getAbsolutePath()).entity(json).build();
			} else {
				return Response.ok(json).build();
			}
		} else {
			throw new InternalServerErrorException();
		}
	}

	protected Response deleteObject(NamedObject object, String methodName, boolean quorumNeeded) throws Exception {
		// Check Quorum
		if (quorumNeeded && !restListener.isQuorum()) {
			throw new NoQuorumException();
		}

		// Validation
		if (object == null) {
			throw new NotFoundException();
		}

		// Send to Cluster
		if (restListener.handleStateOperation(null, methodName, new Object[] { object.getName() },
				new Class[] { String.class })) {
			// TODO Why do we return the deleted object here?
			return Response.ok(jsonConverter.toJson(object)).build();
		} else {
			throw new InternalServerErrorException();
		}
	}
}
