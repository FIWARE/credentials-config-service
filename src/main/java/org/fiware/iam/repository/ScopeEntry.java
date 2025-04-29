package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

	@Id
	@GeneratedValue
	private Long id;

	private String scopeKey;

	@OneToMany(mappedBy = "scopeEntry", cascade = CascadeType.ALL, orphanRemoval = true)
	private Collection<Credential> credentials;

	@OneToOne(cascade = CascadeType.ALL)
	private PresentationDefinition presentationDefinition;
}
