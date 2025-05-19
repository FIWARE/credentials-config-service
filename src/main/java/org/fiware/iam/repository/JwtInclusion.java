package org.fiware.iam.repository;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class JwtInclusion {

	private boolean enabled = true;
	private boolean fullInclusion = false;
	private List<Claim> claimsToInclude = new ArrayList<>();
}
