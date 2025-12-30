package org.fiware.iam.repository;

import io.micronaut.context.annotation.Requires;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;

/**
 * Extension of the {@link ScopeEntryRepository} for the MySql-dialect
 */
@Requires(property = "datasources.default.dialect", value = "H2")
@JdbcRepository(dialect = Dialect.H2)
public interface H2ScopeEntryRepository extends ScopeEntryRepository {
}