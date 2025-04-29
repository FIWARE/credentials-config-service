package org.fiware.iam.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.DataType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.fiware.iam.ccs.model.TrustedParticipantsListEndpointVO;

import java.util.Collection;

@Introspected
@Getter
@Setter
@MappedEntity
public class ScopeEntry {

	@Id
	@GeneratedValue
	private Long id;

	private String scopeKey;

	@Relation(value = Relation.Kind.MANY_TO_ONE)
	@JsonIgnore
	private Service service;

	@TypeDef(type = DataType.JSON)
	private Collection<Credential> credentials;

	@TypeDef(type = DataType.JSON)
	private PresentationDefinition presentationDefinition;

}
