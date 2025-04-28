package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Collection;

@Introspected
@Accessors(chain = true)
@Data
@Entity
@EqualsAndHashCode
public class ScopeEntry {

	Collection<Credential> credentials;

	PresentationDefinition presentationDefinition;
}
