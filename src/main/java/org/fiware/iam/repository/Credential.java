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
@Accessors(chain = true)
@Entity
@EqualsAndHashCode(exclude = "service")
@ToString(exclude = "service")
public class Credential {

	@GeneratedValue
	@Id
	private Integer id;

	private String credentialType;

	@OneToMany(mappedBy = "credential", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EndpointEntry> trustedIssuersLists;

	@OneToMany(mappedBy = "credential", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EndpointEntry> trustedParticipantsLists;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_id")
	private Service service;




}
