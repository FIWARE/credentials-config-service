package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

@Introspected
@Data
@Entity
@Accessors(chain = true)
public class FormatObject {

	@Id
	@GeneratedValue
	private Long id;

	@ElementCollection
	private Collection<String> alg;

	@ElementCollection
	private Collection<String> proofType;
}
