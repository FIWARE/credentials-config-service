package org.fiware.iam.repository;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Type of the authorization redirect to be returned
 */
public enum AuthorizationType {

	FRONTEND_V2,
	DEEPLINK;
}
