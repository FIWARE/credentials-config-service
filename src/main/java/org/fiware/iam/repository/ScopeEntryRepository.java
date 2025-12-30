package org.fiware.iam.repository;

import io.micronaut.data.repository.CrudRepository;

/**
 * Extension of the base repository to support {@link ScopeEntry}
 */
public interface ScopeEntryRepository extends CrudRepository<ScopeEntry, Long> {

    /**
     * Deletes all scope entries associated with a specific service.
     * Micronaut will generate: DELETE FROM scope_entry WHERE service_id = ?
     */
    void deleteByService(Service service);
}