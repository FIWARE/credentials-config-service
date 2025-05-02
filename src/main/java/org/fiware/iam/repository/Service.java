package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

/**
 * Data entity representing a service
 */
@Introspected
@Getter
@Setter
@MappedEntity
@EqualsAndHashCode
@ToString
public class Service {

	@Id
	private String id;

	private String defaultOidcScope;

	@Relation(value = Relation.Kind.ONE_TO_MANY, mappedBy = "service", cascade = Relation.Cascade.ALL)
	private Collection<ScopeEntry> oidcScopes;
}