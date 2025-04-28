package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * Data entity to map Constraints
 */
@Introspected
@Data
@Accessors(chain = true)
@Entity
public class Constraints {

	private Collection<Field> fields;
}
