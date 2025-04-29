package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Map;

/**
 * Data entity to map a PresentationDefinition
 */
@Introspected
@Data
@Entity
@Accessors(chain = true)
public class PresentationDefinition {

	@Id
	private String  id;
	private String name;
	private String purpose;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private Collection<InputDescriptor> inputDescriptors;

	@OneToMany(mappedBy = "presentationDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
	@MapKey(name = "formatKey")
	private Map<String, FormatObject> format;
}

