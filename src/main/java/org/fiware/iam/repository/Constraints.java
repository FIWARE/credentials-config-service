package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

/**
 * Data entity to map Constraints
 */
@Introspected
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Constraints {

	private Collection<Field> fields;

}
