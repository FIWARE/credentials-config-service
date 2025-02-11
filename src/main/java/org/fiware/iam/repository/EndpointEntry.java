package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Entity to represent a single endpoint for either the trusted-issuers-list or the trusted-participants-list
 */
@Introspected
@Data
@Accessors(chain = true)
@Entity
public class EndpointEntry {

	private Integer id;

	private EndpointType type;

	private ListType listType = ListType.EBSI;

	private String endpoint;
}
