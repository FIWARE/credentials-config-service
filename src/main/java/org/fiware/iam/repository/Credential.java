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
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Data entity to map a credential
 */
@Introspected
@Getter
@Setter
public class Credential {

	private String credentialType;
	private boolean verifyHolder;
	private String holderClaim;
	private List<EndpointEntry> trustedLists;

}