package org.fiware.iam.repository;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Claim {

	private String originalKey;
	private String newKey;
}
