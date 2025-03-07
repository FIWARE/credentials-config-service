package org.fiware.iam.repository;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Type of the {@link EndpointEntry} list, either being in EBSI format or in Gaia-X
 */
public enum ListType {

	GAIA_X("gaia-x"),
	EBSI("ebsi");

	private final String value;

	private ListType(String value) {
		this.value = value;
	}

	public static ListType toEnum(String value) {
		return Arrays.stream(values())
				.filter(e -> e.value.equals(value))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException(String.format("Unknown value %s", value)));
	}

	@JsonValue
	public String getValue() {
		return value;
	}
}
