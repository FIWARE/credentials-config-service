package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * Data entity to map an InputDescriptor
 */
@Introspected
@Data
@Accessors(chain = true)
@Entity
public class InputDescriptor {

	private String  id;
	private String name;
	private String purpose;
	private Constraints constraints;
}
