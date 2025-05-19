package org.fiware.iam.repository;

import lombok.Data;

import java.util.Collection;

@Data
public class FormatObject {

	private Collection<String> alg;
	private Collection<String> proofType;

}