package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * Data entity to map a PresentationDefinition
 */
@Introspected
@Data
@Accessors(chain = true)
@Entity
public class PresentationDefinition {

	private String  id;
	private String name;
	private String purpose;
	private Collection<InputDescriptor> inputDescriptors;
}

