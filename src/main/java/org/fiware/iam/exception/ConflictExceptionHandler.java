package org.fiware.iam.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.fiware.iam.ccs.model.ProblemDetailsVO;

import java.net.URI;

/**
 * Catch all {@link ConflictException} and translate them into proper 409 responses.
 */
@Produces
@Singleton
@Requires(classes = { ConflictException.class, ExceptionHandler.class })
@Slf4j
public class ConflictExceptionHandler
		implements ExceptionHandler<ConflictException, HttpResponse<ProblemDetailsVO>> {

	@Override public HttpResponse<ProblemDetailsVO> handle(HttpRequest request, ConflictException exception) {
		log.debug("Received  a conflict for {}.", request, exception);
		return HttpResponse.status(HttpStatus.CONFLICT).body(new ProblemDetailsVO()
				.status(HttpStatus.CONFLICT.getCode())
				.detail(exception.getLocalizedMessage())
				.instance(URI.create(exception.getEntityId()))
				.title("Conflict."));
	}
}
