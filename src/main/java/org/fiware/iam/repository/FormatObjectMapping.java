package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.sql.JoinColumn;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Entity
@Accessors(chain = true)
@Introspected
public class FormatObjectMapping {

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private String formatKey;

	@ManyToOne
	@JoinColumn(name = "presentation_definition_id")
	private PresentationDefinition presentationDefinition;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private FormatObject formatObject;

}