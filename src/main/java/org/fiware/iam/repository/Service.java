package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Map;

/**
 * Data entity representing a service
 */
@Introspected
@Accessors(chain = true)
@Data
@Entity
@EqualsAndHashCode
public class Service {

	@Id
	private String id;

	private String defaultOidcScope;

	@OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
	@MapKey(name = "scopeKey")
	private Map<String, ScopeEntry> oidcScopes;
}