package org.fiware.iam.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.fiware.iam.ccs.api.ServiceApiTestClient;
import org.fiware.iam.ccs.api.ServiceApiTestSpec;
import org.fiware.iam.ccs.model.*;
import org.fiware.iam.repository.JwtInclusion;
import org.fiware.iam.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(packages = {"org.fiware.iam.rest"})
public class ServiceApiControllerTest implements ServiceApiTestSpec {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Inject
	public ServiceApiTestClient testClient;

	@Inject
	public ServiceRepository serviceRepository;

	@Inject
	public DataSource dataSource;

	private ServiceVO theService;
	private List<String> expectedScopes;
	private int pageSize;
	private int pageNumber;

	@BeforeEach
	public void cleanUp() throws SQLException {
		try (Connection conn = dataSource.getConnection();
			 Statement stmt = conn.createStatement()) {
			stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
			stmt.execute("TRUNCATE TABLE service");
			stmt.execute("TRUNCATE TABLE scope_entry");
			stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
		}
	}

	@Override
	public void createService201() throws Exception {
		HttpResponse<?> creationResponse = testClient.createService(theService);
		assertEquals(HttpStatus.CREATED, creationResponse.getStatus(), "The service should have been created.");
		assertTrue(creationResponse.getHeaders().contains("Location"), "Id should be returned as location header.");
		String location = creationResponse.header("Location");
		if (theService.getId() != null) {
			assertTrue(location.endsWith(theService.getId()), "The provided id should be used.");
		}
	}

	@ParameterizedTest
	@MethodSource("validServices")
	public void createService201(ServiceVO serviceVO) throws Exception {
		theService = serviceVO;
		createService201();
	}

	private static ServiceVO getEmptyService() {
		ServiceScopesEntryVO serviceScopesEntryVO =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO = CredentialVOTestExample.build();
		List<CredentialVO> credentialVOS = new ArrayList<>(serviceScopesEntryVO.getCredentials());
		credentialVOS.add(credentialVO);
		serviceScopesEntryVO.credentials(credentialVOS);
		ServiceVO serviceVO = ServiceVOTestExample.build();
		serviceVO.setDefaultOidcScope("test-oidc-scope");
		serviceVO.setOidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO));
		return serviceVO;
	}

	private static void addToScopeEntry(ServiceScopesEntryVO scopesEntryVO, CredentialVO credentialVO) {
		List<CredentialVO> credentialVOS = new ArrayList<>(scopesEntryVO.getCredentials());
		credentialVOS.add(credentialVO);
		scopesEntryVO.credentials(credentialVOS);
	}

	private static Stream<Arguments> validServices() {
		// Empty credential
		ServiceVO serviceVO = getEmptyService();

		// 2 - Credential with type
		ServiceScopesEntryVO serviceScopesEntryVO2 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO2 = CredentialVOTestExample.build().type("my-credential");
		addToScopeEntry(serviceScopesEntryVO2, credentialVO2);

		ServiceVO serviceVO2 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO2));
		serviceVO2.setDefaultOidcScope("test-oidc-scope");

		// 3 - Credential with type + TIL entry
		ServiceScopesEntryVO serviceScopesEntryVO3 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO3 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedIssuersLists(List.of("http://til.de"));
		addToScopeEntry(serviceScopesEntryVO3, credentialVO3);
		ServiceVO serviceVO3 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO3));
		serviceVO3.setDefaultOidcScope("test-oidc-scope");

		// 4 - Credential with type + 2 TIL entries
		ServiceScopesEntryVO serviceScopesEntryVO4 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO4 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedIssuersLists(List.of("http://til.de", "http://another-til.de"));
		addToScopeEntry(serviceScopesEntryVO4, credentialVO4);
		ServiceVO serviceVO4 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO4));
		serviceVO4.setDefaultOidcScope("test-oidc-scope");

		// 5 - Credential with type + TIL entry + TIR entry
		ServiceScopesEntryVO serviceScopesEntryVO5 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO5 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedIssuersLists(List.of("http://til.de"))
				.trustedParticipantsLists(List.of(new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));
		addToScopeEntry(serviceScopesEntryVO5, credentialVO5);
		ServiceVO serviceVO5 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO5));
		serviceVO5.setDefaultOidcScope("test-oidc-scope");

		// 6 - Credential with type + TIL entry + 2 TIR entries
		ServiceScopesEntryVO serviceScopesEntryVO6 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO6 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedIssuersLists(List.of("http://til.de"))
				.trustedParticipantsLists(List.of(
						new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI),
						new TrustedParticipantsListEndpointVO().url("http://another-tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));
		addToScopeEntry(serviceScopesEntryVO6, credentialVO6);
		ServiceVO serviceVO6 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO6));
		serviceVO6.setDefaultOidcScope("test-oidc-scope");

		// 7 - Credential with type + 2 TIR entries
		ServiceScopesEntryVO serviceScopesEntryVO7 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO7 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedParticipantsLists(List.of(
						new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI),
						new TrustedParticipantsListEndpointVO().url("http://another-tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));
		addToScopeEntry(serviceScopesEntryVO7, credentialVO7);
		ServiceVO serviceVO7 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO7));
		serviceVO7.setDefaultOidcScope("test-oidc-scope");

		// 8 - 2 Credentials with type (2 TIR entries / 1 TIR + 1 TIL entry)
		ServiceScopesEntryVO serviceScopesEntryVO8 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO8_1 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedParticipantsLists(List.of(
						new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI),
						new TrustedParticipantsListEndpointVO().url("http://another-tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));
		CredentialVO credentialVO8_2 = CredentialVOTestExample.build()
				.type("another-credential")
				.trustedIssuersLists(List.of("til.de"))
				.trustedParticipantsLists(List.of(new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));
		addToScopeEntry(serviceScopesEntryVO8, credentialVO8_1);
		addToScopeEntry(serviceScopesEntryVO8, credentialVO8_2);
		ServiceVO serviceVO8 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO8));
		serviceVO8.setDefaultOidcScope("test-oidc-scope");

		// 9 - 2 OIDC scopes, each with different credentials
		ServiceScopesEntryVO serviceScopesEntryVO9_1 =
				ServiceScopesEntryVOTestExample.build();
		ServiceScopesEntryVO serviceScopesEntryVO9_2 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO9_1 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedIssuersLists(List.of("http://til.de"))
				.trustedParticipantsLists(List.of(new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));
		addToScopeEntry(serviceScopesEntryVO9_1, credentialVO9_1);
		CredentialVO credentialVO9_2 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedIssuersLists(List.of("http://til.de"))
				.trustedParticipantsLists(List.of(
						new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI),
						new TrustedParticipantsListEndpointVO().url("http://another-tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));
		CredentialVO credentialVO9_3 = CredentialVOTestExample.build()
				.type("another-credential")
				.trustedIssuersLists(List.of("http://til.de"))
				.trustedParticipantsLists(List.of(
						new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI),
						new TrustedParticipantsListEndpointVO().url("http://another-tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));
		addToScopeEntry(serviceScopesEntryVO9_2, credentialVO9_2);
		addToScopeEntry(serviceScopesEntryVO9_2, credentialVO9_3);
		ServiceVO serviceVO9 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO9_1, "another-oidc-scope", serviceScopesEntryVO9_2));
		serviceVO9.setDefaultOidcScope("test-oidc-scope");


		// 10 - Credential with holder verification
		ServiceScopesEntryVO serviceScopesEntryV10 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialV10 = CredentialVOTestExample.build()
				.type("my-credential")
				.holderVerification(HolderVerificationVOTestExample.build()
						.enabled(true)
						.claim("theHolder"));
		addToScopeEntry(serviceScopesEntryV10, credentialV10);
		ServiceVO serviceV10 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryV10));
		serviceV10.setDefaultOidcScope("test-oidc-scope");

		// 11 - Credential with participants list in old format
		ServiceScopesEntryVO serviceScopesEntryV11 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialV11 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedIssuersLists(List.of("http://til.de"))
				.trustedParticipantsLists(List.of("http://tir.de"));
		addToScopeEntry(serviceScopesEntryV11, credentialV11);
		ServiceVO serviceV11 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryV11));
		serviceV11.setDefaultOidcScope("test-oidc-scope");


		// 12 - Service with a presentation definition
		ServiceScopesEntryVO serviceScopesEntryV12 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialV12 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedIssuersLists(List.of("http://til.de"))
				.trustedParticipantsLists(List.of("http://tir.de"));
		addToScopeEntry(serviceScopesEntryV12, credentialV12);
		PresentationDefinitionVO presentationDefinitionVO = PresentationDefinitionVOTestExample.build();
		Map<String, Object> formatVO = new HashMap<>();
		formatVO.put("vc+sd-jwt", Map.of("alg", List.of("ES256")));
		presentationDefinitionVO.setFormat(formatVO);
		serviceScopesEntryV12.setPresentationDefinition(presentationDefinitionVO);
		ServiceVO serviceV12 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryV12));
		serviceV12.setDefaultOidcScope("test-oidc-scope");

		// 13 - Service with jwt mapping
		ServiceScopesEntryVO serviceScopesEntryV13 =
				ServiceScopesEntryVOTestExample.build();

		CredentialVO credentialV13 = CredentialVOTestExample.build()
				.type("my-credential")
				.jwtInclusion(JwtInclusionVOTestExample
						.build()
						.claimsToInclude(List.of(ClaimVOTestExample.build())));
		addToScopeEntry(serviceScopesEntryV13, credentialV13);
		ServiceVO serviceV13 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryV13));
		serviceV13.setDefaultOidcScope("test-oidc-scope");

		// 14 - Service with jwt mapping
		ServiceScopesEntryVO serviceScopesEntryV14 =
				ServiceScopesEntryVOTestExample.build();

		CredentialVO credentialV14 = CredentialVOTestExample.build()
				.type("my-credential")
				.jwtInclusion(JwtInclusionVOTestExample
						.build()
						.fullInclusion(true)
						.claimsToInclude(List.of(ClaimVOTestExample.build())));
		addToScopeEntry(serviceScopesEntryV14, credentialV14);
		ServiceVO serviceV14 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryV14));
		serviceV14.setDefaultOidcScope("test-oidc-scope");

		// 15 - Service with dcql
		ServiceScopesEntryVO serviceScopesEntryV15 =
				ServiceScopesEntryVOTestExample.build();
		serviceScopesEntryV15.setDcql(DCQLVOTestExample.build());
		CredentialVO credentialV15 = CredentialVOTestExample.build()
				.type("my-credential");
		addToScopeEntry(serviceScopesEntryV15, credentialV15);
		ServiceVO serviceV15 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryV15));
		serviceV15.setDefaultOidcScope("test-oidc-scope");


		return Stream.of(
				// Empty credential
				Arguments.of(serviceVO,
						List.of("VerifiableCredential")),
				// 2 - Credential with type
				Arguments.of(serviceVO2,
						List.of("my-credential")),
				// 3 - Credential with type + TIL entry
				Arguments.of(serviceVO3,
						List.of("my-credential")),
				// 4 - Credential with type + 2 TIL entries
				Arguments.of(serviceVO4,
						List.of("my-credential")),
				// 5 - Credential with type + TIL entry + TIR entry
				Arguments.of(serviceVO5,
						List.of("my-credential")),
				// 6 - Credential with type + TIL entry + 2 TIR entries
				Arguments.of(serviceVO6,
						List.of("my-credential")),
				// 7 - Credential with type + 2 TIR entries
				Arguments.of(serviceVO7,
						List.of("my-credential")),
				// 8 - 2 Credentials with type (2 TIR entries / 1 TIR + 1 TIL entry)
				Arguments.of(serviceVO8,
						List.of("my-credential", "another-credential")),
				// 9 - 2 OIDC scopes, each with different credentials
				Arguments.of(serviceVO9,
						List.of("my-credential")),
				// 10 - Credential with holder verification
				Arguments.of(serviceV10,
						List.of("my-credential")),
				// 11 - Credential with participants list in old format
				Arguments.of(serviceV11,
						List.of("my-credential")),
				// 12 - Credential with participants list in old format
				Arguments.of(serviceV12,
						List.of("my-credential")),
				// 13 -  Service with jwt mapping
				Arguments.of(serviceV13,
						List.of("my-credential")),
				// 14 -  Service with jwt mapping full inclusion
				Arguments.of(serviceV14,
						List.of("my-credential")),
				// 15 -  Service with jwt mapping full inclusion
				Arguments.of(serviceV15,
						List.of("my-credential"))
		);
	}

	@Override
	public void createService400() throws Exception {
		try {
			testClient.createService(theService);
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatus(), "The service should not have been created.");
			return;
		}
		fail("The creation attempt should fail for an invalid service.");
	}

	@ParameterizedTest
	@MethodSource("invalidServices")
	public void createService400(ServiceVO serviceVO) throws Exception {
		theService = serviceVO;
		createService400();
	}

	private static Stream<Arguments> invalidServices() {
		// Credential with empty type
		ServiceScopesEntryVO serviceScopesEntryVO =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO = CredentialVOTestExample.build().type(null);
		addToScopeEntry(serviceScopesEntryVO, credentialVO);
		ServiceVO serviceVO = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO));
		serviceVO.setDefaultOidcScope("test-oidc-scope");

		// 2 - 2 Credentials, but 1 has empty type
		ServiceScopesEntryVO serviceScopesEntryVO2 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO2_1 = CredentialVOTestExample.build();
		CredentialVO credentialVO2_2 = CredentialVOTestExample.build().type(null);
		addToScopeEntry(serviceScopesEntryVO2, credentialVO2_1);
		addToScopeEntry(serviceScopesEntryVO2, credentialVO2_2);
		ServiceVO serviceVO2 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO2));
		serviceVO2.setDefaultOidcScope("test-oidc-scope");

		// 3 - 1 OIDC scope/Credential, but no default OIDC scope
		ServiceScopesEntryVO serviceScopesEntryVO3 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO3 = CredentialVOTestExample.build().type(null);
		addToScopeEntry(serviceScopesEntryVO3, credentialVO3);
		ServiceVO serviceVO3 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO3));
		serviceVO3.setDefaultOidcScope(null);

		// 4 - flat claims - duplicate keys included
		ServiceScopesEntryVO scopesEntryVO04 =
				ServiceScopesEntryVOTestExample.build();
		JwtInclusionVO jwtInclusionVO = JwtInclusionVOTestExample.build()
				.claimsToInclude(List.of(ClaimVOTestExample.build()
						.originalKey("else")
						.newKey("test")));
		CredentialVO credentialVO4_01 = CredentialVOTestExample.build()
				.type("MyCredential")
				.jwtInclusion(jwtInclusionVO);
		CredentialVO credentialVO4_02 = CredentialVOTestExample.build()
				.type("MyOtherCredential")
				.jwtInclusion(jwtInclusionVO);
		addToScopeEntry(scopesEntryVO04, credentialVO4_01);
		addToScopeEntry(scopesEntryVO04, credentialVO4_02);
		ServiceVO serviceVO4 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", scopesEntryVO04));
		serviceVO4.setDefaultOidcScope(null);

		// 5 - duplicate keys included
		ServiceScopesEntryVO scopesEntryVO05 =
				ServiceScopesEntryVOTestExample.build();
		JwtInclusionVO jwtInclusionVO_05 = JwtInclusionVOTestExample.build()
				.claimsToInclude(List.of(
						ClaimVOTestExample.build()
								.originalKey("else")
								.newKey("test"),
						ClaimVOTestExample.build()
								.originalKey("test")
				));
		CredentialVO credentialVO5 = CredentialVOTestExample.build()
				.type("MyCredential")
				.jwtInclusion(jwtInclusionVO_05);
		addToScopeEntry(scopesEntryVO05, credentialVO5);
		ServiceVO serviceVO5 = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", scopesEntryVO05));
		serviceVO5.setDefaultOidcScope(null);

		return Stream.of(
				// Service with empty OIDC scopes
				Arguments.of(ServiceVOTestExample.build().oidcScopes(null)),
				// Service with ID but empty OIDC scopes
				Arguments.of(ServiceVOTestExample.build().id("my-service").oidcScopes(null)),
				// Credential with empty type
				Arguments.of(serviceVO),
				// 2 - 2 Credentials, but 1 has empty type
				Arguments.of(serviceVO2),
				// 3 - 1 OIDC scope/Credential, but no default OIDC scope
				Arguments.of(serviceVO3),
				// 4 - flat claims - duplicate keys included
				Arguments.of(serviceVO4),
				//  5 - duplicate keys included
				Arguments.of(serviceVO5)
		);
	}

	@Test
	@Override
	public void createService409() throws Exception {
		ServiceVO serviceToBeCreated = getEmptyService().id("my-service");
		assertEquals(HttpStatus.CREATED, testClient.createService(serviceToBeCreated).getStatus(),
				"The initial creation should succeed.");
		try {
			testClient.createService(serviceToBeCreated);
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.CONFLICT, e.getStatus(), "Creating another service with the same id should fail.");
			return;
		}
		fail("Creating another service with the same id should fail.");

	}

	@Test
	@Override
	public void deleteServiceById204() throws Exception {
		ServiceVO serviceToBeCreated = getEmptyService().id("my-service");
		assertEquals(HttpStatus.CREATED, testClient.createService(serviceToBeCreated).getStatus(),
				"The initial creation should succeed.");

		HttpResponse<?> deletionResponse = testClient.deleteServiceById("my-service");
		assertEquals(HttpStatus.NO_CONTENT, deletionResponse.getStatus(), "The service should have been deleted.");
		assertFalse(serviceRepository.existsById("my-service"), "The service should have been deleted");

	}

	@Test
	@Override
	public void deleteServiceById404() throws Exception {
		HttpResponse<?> deletionResponse = testClient.deleteServiceById("my-service");
		assertEquals(HttpStatus.NOT_FOUND, deletionResponse.getStatus(), "The deletion request should not succeed.");
	}

	@Override
	public void getScopeForService200() throws Exception {
		// stable id, so that we can retrieve
		theService.setId("my-service");
		assertEquals(HttpStatus.CREATED, testClient.createService(theService).getStatus(),
				"The service should be initially created.");
		HttpResponse<List<String>> scopeResponse = testClient.getScopeForService("my-service", null);
		List<String> returnedScope = scopeResponse.body();
		assertTrue(returnedScope.size() == expectedScopes.size() && returnedScope.containsAll(
						expectedScopes) && expectedScopes.containsAll(returnedScope),
				"All expected scopes should have been returned.");
	}

	@ParameterizedTest
	@MethodSource("validServices")
	public void getScopeForService200(ServiceVO serviceVO, List<String> scopes) throws Exception {
		theService = serviceVO;
		expectedScopes = scopes;
		getScopeForService200();
	}

	@Test
	@Override
	public void getScopeForService404() throws Exception {
		assertEquals(HttpStatus.NOT_FOUND, testClient.getScopeForService("my-service", null).getStatus(),
				"If no such service exists, a 404 should be returned.");
	}

	@Override
	public void getService200() throws Exception {
		HttpResponse<ServiceVO> theServiceResponse = testClient.getService(theService.getId());
		assertEquals(HttpStatus.OK, theServiceResponse.getStatus(), "The service should be responded with status OK.");
		assertServiceVOsEqual(theService, theServiceResponse.body());
	}

	@ParameterizedTest
	@MethodSource("validServices")
	public void getService200(ServiceVO serviceVO) throws Exception {
		serviceVO.setId(Optional.ofNullable(serviceVO.getId()).orElse(UUID.randomUUID().toString()));
		theService = serviceVO;
		assertEquals(HttpStatus.CREATED, testClient.createService(serviceVO).getStatus(),
				"The service should have been initially created.");
		getService200();
	}

	@Test
	@Override
	public void getService404() throws Exception {
		assertEquals(HttpStatus.NOT_FOUND, testClient.getService("my-service").status(),
				"If no service exists, a 404 should be returned.");
	}

	@Test
	@Override
	public void getServices200() throws Exception {
		HttpResponse<ServicesVO> servicesVOHttpResponse = testClient.getServices(null, null);
		assertEquals(HttpStatus.OK, servicesVOHttpResponse.status(),
				"If no services exist, an empty list should be returned.");
		assertTrue(servicesVOHttpResponse.body().getServices().isEmpty(),
				"If no services exist, an empty list should be returned.");

		List<ServiceVO> services = new ArrayList<>();
		for (int i = 10; i < 30; i++) {
			ServiceScopesEntryVO serviceScopesEntryVO =
					ServiceScopesEntryVOTestExample.build();
			CredentialVO credentialVO = CredentialVOTestExample.build();
			addToScopeEntry(serviceScopesEntryVO, credentialVO);

			ServiceVO serviceVO = ServiceVOTestExample.build()
					.id(String.valueOf(i))
					.oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO));
			serviceVO.setDefaultOidcScope("test-oidc-scope");

			assertEquals(HttpStatus.CREATED, testClient.createService(serviceVO).status(),
					"Initial creation should succeed.");
			services.add(serviceVO);
		}
		servicesVOHttpResponse = testClient.getServices(null, null);
		assertEquals(HttpStatus.OK, servicesVOHttpResponse.status(), "The services should have been returned.");
		assertServicesResponse(20, 20, 10, 29, 0, servicesVOHttpResponse.body());

		servicesVOHttpResponse = testClient.getServices(10, null);
		assertEquals(HttpStatus.OK, servicesVOHttpResponse.getStatus(), "The services should have been returned");
		assertServicesResponse(20, 10, 10, 19, 0, servicesVOHttpResponse.body());

		servicesVOHttpResponse = testClient.getServices(10, 1);
		assertEquals(HttpStatus.OK, servicesVOHttpResponse.getStatus(), "The services should have been returned");
		assertServicesResponse(20, 10, 20, 29, 1, servicesVOHttpResponse.body());

	}

	private void assertServicesResponse(int total, int pageSize, int startIndex, int endIndex, int pageNumber,
										ServicesVO servicesVO) {
		assertEquals(total, servicesVO.getTotal(), "The correct total should be returned");
		assertEquals(pageNumber, servicesVO.getPageNumber(), "The correct page should have been returend.");
		assertEquals(pageSize, servicesVO.getPageSize(), "The correct page size should be returned.");
		assertEquals(endIndex - startIndex + 1, servicesVO.getServices().size(),
				"All requested items should be included.");
		assertEquals(String.format("%s", startIndex), servicesVO.getServices().get(0).getId(),
				"The correct start item should be returned.");
		assertEquals(String.format("%s", endIndex), servicesVO.getServices().get(endIndex - startIndex).getId(),
				"The correct end item should be returned.");
	}

	@Override
	public void getServices400() throws Exception {
		try {
			testClient.getServices(pageSize, pageNumber);
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatus(), "Invalid pagination parameters should be rejected.");
			return;
		}
		fail("Invalid pagination parameters should be rejected.");
	}

	@ParameterizedTest
	@MethodSource("invalidPagination")
	public void getServices400(int pageSize, int pageNumber) throws Exception {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		getServices400();
	}

	private static Stream<Arguments> invalidPagination() {
		return Stream.of(Arguments.of(-1, 10),
				Arguments.of(1, -1),
				Arguments.of(0, 10),
				Arguments.of(-1, -1),
				Arguments.of(0, -1));
	}

	@Override
	public void updateService200() throws Exception {
		HttpResponse<ServiceVO> updatedService = testClient.updateService(theService.getId(), theService);
		assertEquals(HttpStatus.OK, updatedService.status(), "The service should have been updated withst status OK.");
		assertServiceVOsEqual(theService, updatedService.body());
	}

	@ParameterizedTest
	@MethodSource("validServices")
	public void updateService200(ServiceVO serviceVO) throws Exception {
		HttpResponse<?> initialCreate = testClient.createService(getEmptyService());
		assertEquals(HttpStatus.CREATED, initialCreate.status(), "The creation should have been succeeded.");
		// id is not allowed to be updated
		theService = serviceVO.id("packet-delivery-service");
		updateService200();
	}

	@Override
	public void updateService400() throws Exception {
		try {
			testClient.updateService(theService.getId(), theService);
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatus(), "Invalid services should be rejected.");
			return;
		}
		fail("Invalid services should be rejected.");
	}

	@ParameterizedTest
	@MethodSource("invalidServices")
	public void updateService400(ServiceVO serviceVO) throws Exception {
		HttpResponse<?> initialCreate = testClient.createService(getEmptyService());
		assertEquals(HttpStatus.CREATED, initialCreate.status(), "The creation should have been succeeded.");
		theService = serviceVO;
		updateService400();
	}

	@Test
	@Override
	public void updateService404() throws Exception {
		ServiceVO serviceVO = getEmptyService();
		assertEquals(HttpStatus.NOT_FOUND, testClient.updateService(serviceVO.getId(), serviceVO).status(),
				"Only existing services can be updated.");
	}

	@Test
	public void updateServiceRemoveScope200() throws Exception {
		ServiceScopesEntryVO entry1 = ServiceScopesEntryVOTestExample.build();
		ServiceScopesEntryVO entry2 = ServiceScopesEntryVOTestExample.build();
		CredentialVO credential1 = CredentialVOTestExample.build();
		CredentialVO credential2 = CredentialVOTestExample.build();
		addToScopeEntry(entry1, credential1);
		addToScopeEntry(entry2, credential2);

		Map<String, ServiceScopesEntryVO> scopes = new HashMap<>();
		scopes.put("scope1", entry1);
		scopes.put("scope2", entry2);

		ServiceVO service = ServiceVOTestExample.build().id("service-remove-scope").oidcScopes(scopes);
		service.setDefaultOidcScope("scope1");

		assertEquals(HttpStatus.CREATED, testClient.createService(service).getStatus(), "Initial creation should succeed.");

		// Remove scope2 and update
		service.setOidcScopes(Map.of("scope1", entry1));

		HttpResponse<ServiceVO> updatedResponse = testClient.updateService(service.getId(), service);
		assertEquals(HttpStatus.OK, updatedResponse.getStatus(), "The service should be updated successfully.");

		// Fetch all services and compare the stored service by id
		HttpResponse<ServicesVO> servicesResponse = testClient.getServices(null, null);
		assertEquals(HttpStatus.OK, servicesResponse.getStatus(), "Fetching services should succeed.");
		ServicesVO servicesVO = servicesResponse.body();
		assertNotNull(servicesVO, "Services response body should not be null.");
		Optional<ServiceVO> opt = servicesVO.getServices().stream()
			.filter(s -> Objects.equals(s.getId(), service.getId()))
			.findFirst();
		assertTrue(opt.isPresent(), "Updated service should be present in the services list.");
		ServiceVO updatedFromList = opt.get();
		assertNotNull(updatedFromList);
		assertEquals(1, updatedFromList.getOidcScopes().size(), "Only one scope should remain after update.");
		assertTrue(updatedFromList.getOidcScopes().containsKey("scope1"), "Remaining scope should be scope1.");
	}

    private void assertServiceVOsEqual(ServiceVO expected, ServiceVO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDefaultOidcScope(), actual.getDefaultOidcScope());
        assertEquals(expected.getOidcScopes().size(), actual.getOidcScopes().size());
        assertTrue(actual.getOidcScopes().keySet().containsAll(expected.getOidcScopes().keySet()));
    }

	public OrderIgnoringCredential fromCredentialVO(CredentialVO credentialVO) {
		OrderIgnoringCredential orderIgnoringCredential = new OrderIgnoringCredential();
		orderIgnoringCredential.setType(credentialVO.getType());
		orderIgnoringCredential.setTrustedParticipantsLists(credentialVO.getTrustedParticipantsLists().stream().map(o -> {
			if (o instanceof String stringEntry) {
				// expand the string to the new format.
				return new TrustedParticipantsListEndpointVO().type(TrustedParticipantsListEndpointVO.Type.EBSI).url(stringEntry);
			} else if (o instanceof Map<?, ?> mapEntry) {
				return OBJECT_MAPPER.convertValue(mapEntry, TrustedParticipantsListEndpointVO.class);
			} else {
				return o;
			}
		}).collect(Collectors.toSet()));
		orderIgnoringCredential.setTrustedIssuersLists(new HashSet<>(credentialVO.getTrustedIssuersLists()));
		orderIgnoringCredential.setHolderVerificationVO(credentialVO.getHolderVerification());
		return orderIgnoringCredential;
	}

	@EqualsAndHashCode
	@ToString
	class OrderIgnoringCredential {

		@Setter
		private String type;
		@Setter
		private Set<Object> trustedParticipantsLists;
		@Setter
		private Set<String> trustedIssuersLists;
		@Setter
		private HolderVerificationVO holderVerificationVO;

	}
}