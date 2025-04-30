package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * Data entity to map a Field
 */
@Introspected
@Getter
@Setter
public class Field {

	private String id;

	private String name;
	private String purpose;
	private Boolean optional;

	private Collection<String> path;
	private Object filter;
}
