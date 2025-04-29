package org.fiware.iam.repository;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Introspected
@Getter
@Setter
public class Format {

	private String formatKey;

	private Collection<String> alg;
	private Collection<String> proofType;

}
