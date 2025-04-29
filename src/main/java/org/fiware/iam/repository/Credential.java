package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Data entity to map a credential
 */
@Introspected
@Data
@Entity
@Accessors(chain = true)
public class Credential {

	@Id
	@GeneratedValue
	private Integer id;
	private String credentialType;

	private boolean verifyHolder;
	private String holderClaim;

	@ManyToOne
	private ScopeEntry scopeEntry;

	@OneToMany(mappedBy = "credential", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EndpointEntry> trustedLists;

}