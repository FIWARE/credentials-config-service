package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Data entity to map a credential
 */
@Introspected
@Data
@Accessors(chain = true)
@Entity
public class Credential {

	private Integer id;

	private String credentialType;

	private List<EndpointEntry> trustedLists;

	private boolean verifyHolder;

	private boolean requireCompliance;

	private String holderClaim;
}