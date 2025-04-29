package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Entity to represent a single endpoint for either the trusted-issuers-list or the trusted-participants-list
 */
@Introspected
@Data
@Entity
@Accessors(chain = true)
public class EndpointEntry {

	@Id
	@GeneratedValue
	private Integer id;

	@Enumerated(EnumType.STRING)
	private EndpointType type;

	@Enumerated(EnumType.STRING)
	private ListType listType = ListType.EBSI;

	private String endpoint;

	@ManyToOne
	private Credential credential;
}
