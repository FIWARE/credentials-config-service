package org.fiware.iam.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Data entity to map an InputDescriptor
 */
@Introspected
@Getter
@Setter
public class InputDescriptor {

	private String name;
	private String purpose;

	private Constraints constraints;

}
