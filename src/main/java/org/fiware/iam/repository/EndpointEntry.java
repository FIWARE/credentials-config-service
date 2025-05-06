package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity to represent a single endpoint for either the trusted-issuers-list or the trusted-participants-list
 */
@Introspected
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class EndpointEntry {

	private EndpointType type;
	private ListType listType = ListType.EBSI;
	private String endpoint;

}
