package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * Data entity to map a Field
 */
@Introspected
@Data
@Accessors(chain = true)
@Entity
public class Field {

	private String id;
	private String name;
	private String purpose;
	private Boolean optional;
	private Collection<String> path;
}
