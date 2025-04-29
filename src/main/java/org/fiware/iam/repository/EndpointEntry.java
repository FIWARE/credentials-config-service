package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity to represent a single endpoint for either the trusted-issuers-list or the trusted-participants-list
 */
@Introspected
@Getter
@Setter
public class EndpointEntry {

	private EndpointType type;
	private ListType listType = ListType.EBSI;
	private String endpoint;

}
