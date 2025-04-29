package org.fiware.iam.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * Data entity to map a PresentationDefinition
 */
@Introspected
@Getter
@Setter
public class PresentationDefinition {

	private String id;
	private String name;
	private String purpose;

	private Collection<InputDescriptor> inputDescriptors;
	private Collection<Format> format;

}

