package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

/**
 * Data entity to map a PresentationDefinition
 */
@Introspected
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PresentationDefinition {

	private String id;
	private String name;
	private String purpose;

	private Collection<InputDescriptor> inputDescriptors;
	private Collection<Format> format;

}

