package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@Introspected
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Format {

	private String formatKey;

	private Collection<String> alg;
	private Collection<String> proofType;

}
