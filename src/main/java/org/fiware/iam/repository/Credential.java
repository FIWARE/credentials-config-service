package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Data entity to map a credential
 */
@Introspected
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Credential {

	private String credentialType;
	private boolean verifyHolder;
	private String holderClaim;
	private List<EndpointEntry> trustedLists;

}