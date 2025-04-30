package org.fiware.iam.repository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

/**
 * Extension of the base repository to support {@link Service}
 */
public interface ServiceRepository extends PageableRepository<Service, String> {

	@Join(value = "oidcScopes", type = Join.Type.LEFT_FETCH)
	Optional<Service> findById(@NonNull String id);

	@Join(value = "oidcScopes", type = Join.Type.LEFT_FETCH)
	@NonNull Page<Service> findAll(@NonNull Pageable pageable);
}
