package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

/**
 * Data entity to map a Field
 */
@Introspected
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Field {

	private String id;

	private String name;
	private String purpose;
	private Boolean optional;

	private Collection<String> path;
	private Object filter;
}
