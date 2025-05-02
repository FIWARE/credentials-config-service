package org.fiware.iam;

import org.fiware.iam.ccs.model.*;
import org.fiware.iam.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ServiceMapperTest {

	private ServiceMapper serviceMapper = new ServiceMapperImpl();

	@Test
	void testMapToService() {
		// Create ServiceVO
		ServiceScopesEntryVO serviceScopesEntryVO_1 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO_1 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedIssuersLists(List.of("http://til.de"))
				.trustedParticipantsLists(List.of(new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));

		List<CredentialVO> credentialVOS = new ArrayList<>(serviceScopesEntryVO_1.getCredentials());
		credentialVOS.add(credentialVO_1);
		serviceScopesEntryVO_1.credentials(credentialVOS);
		ServiceVO serviceVO = ServiceVOTestExample.build().oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO_1));
		serviceVO.setDefaultOidcScope("test-oidc-scope");

		// Map to Service
		Service service = serviceMapper.map(serviceVO);

		// Asserts
		assertEquals("test-oidc-scope", service.getDefaultOidcScope(),
				"should have equal default OIDC scope");

		Collection<ScopeEntry> serviceScopes = service.getOidcScopes();
		assertEquals(1, serviceScopes.size(), "Collection of service scopes should have 1 entry");
		ScopeEntry scopeEntry = serviceScopes.stream().toList().get(0);
		assertEquals("test-oidc-scope", scopeEntry.getScopeKey(), "ServiceScope should have correct name");
		Collection<Credential> credentials = scopeEntry.getCredentials();
		assertEquals(1, credentials.size(), "Collection of credentials should have 1 entry");

		Credential credential = credentials.iterator().next();
		assertEquals("my-credential", credential.getCredentialType(),
				"Credential should have correct type");

		List<EndpointEntry> endpointEntries = credential.getTrustedLists();
		assertEquals(2, endpointEntries.size(), "Trusted lists should have 2 entries");

		for (EndpointEntry endpointEntry : endpointEntries) {
			if (endpointEntry.getType() == EndpointType.TRUSTED_ISSUERS) {
				assertEquals("http://til.de", endpointEntry.getEndpoint(),
						"Trusted issuer endpoint should have correct URL");
			} else if (endpointEntry.getType() == EndpointType.TRUSTED_PARTICIPANTS) {
				assertEquals("http://tir.de", endpointEntry.getEndpoint(),
						"Trusted participants endpoint should have correct URL");
			} else {
				fail("Invalid EndpointEntry type");
			}
		}
	}

	@Test
	void testMapToServiceMultipleScopesAndCredentials() {
		// Create ServiceVO
		ServiceScopesEntryVO serviceScopesEntryVO_1 =
				ServiceScopesEntryVOTestExample.build();
		ServiceScopesEntryVO serviceScopesEntryVO_2 =
				ServiceScopesEntryVOTestExample.build();
		CredentialVO credentialVO_1 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedIssuersLists(List.of("http://til.de"))
				.trustedParticipantsLists(List.of(new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));

		List<CredentialVO> credentialVOS_1 = new ArrayList<>(serviceScopesEntryVO_1.getCredentials());
		credentialVOS_1.add(credentialVO_1);
		serviceScopesEntryVO_1.credentials(credentialVOS_1);

		CredentialVO credentialVO_2 = CredentialVOTestExample.build()
				.type("my-credential")
				.trustedIssuersLists(List.of("http://til.de"))
				.trustedParticipantsLists(List.of(
						new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI),
						new TrustedParticipantsListEndpointVO().url("http://another-tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));
		CredentialVO credentialVO_3 = CredentialVOTestExample.build()
				.type("another-credential")
				.trustedIssuersLists(List.of("http://til.de"))
				.trustedParticipantsLists(List.of(
						new TrustedParticipantsListEndpointVO().url("http://tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI),
						new TrustedParticipantsListEndpointVO().url("http://another-tir.de").type(TrustedParticipantsListEndpointVO.Type.EBSI)));

		List<CredentialVO> credentialVOS_2 = new ArrayList<>(serviceScopesEntryVO_2.getCredentials());
		credentialVOS_2.add(credentialVO_2);
		credentialVOS_2.add(credentialVO_3);
		serviceScopesEntryVO_2.credentials(credentialVOS_2);
		ServiceVO serviceVO = ServiceVOTestExample.build()
				.oidcScopes(Map.of("test-oidc-scope", serviceScopesEntryVO_1, "another-oidc-scope", serviceScopesEntryVO_2));
		serviceVO.setDefaultOidcScope("test-oidc-scope");

		// Map to Service
		Service service = serviceMapper.map(serviceVO);

		// Asserts
		assertEquals("test-oidc-scope", service.getDefaultOidcScope(),
				"should have correct default OIDC scope");

		Collection<ScopeEntry> serviceScopes = service.getOidcScopes();
		assertEquals(2, serviceScopes.size(),
				"Collection of service scopes should have 2 entries");

		Collection<Credential> credentials = null;
		for (ScopeEntry serviceScope : serviceScopes) {
			credentials = serviceScope.getCredentials();
			if (credentials.size() == 1) {
				assertEquals("test-oidc-scope", serviceScope.getScopeKey(),
						"ServiceScope should have correct name");

				Credential credential = credentials.iterator().next();
				assertEquals("my-credential", credential.getCredentialType(),
						"Credential should have correct type");

				List<EndpointEntry> endpointEntries = credential.getTrustedLists();
				assertEquals(2, endpointEntries.size(), "Trusted lists should have 2 entries");

				for (EndpointEntry endpointEntry : endpointEntries) {
					if (endpointEntry.getType() == EndpointType.TRUSTED_ISSUERS) {
						assertEquals("http://til.de", endpointEntry.getEndpoint(),
								"Trusted issuer endpoint should have correct URL");
					} else if (endpointEntry.getType() == EndpointType.TRUSTED_PARTICIPANTS) {
						assertEquals("http://tir.de", endpointEntry.getEndpoint(),
								"Trusted participants endpoint should have correct URL");
					} else {
						fail("Invalid EndpointEntry type");
					}
				}
			} else if (credentials.size() == 2) {
				assertEquals("another-oidc-scope", serviceScope.getScopeKey(),
						"ServiceScope should have correct name");
				for (Credential credential : credentials) {
					assertTrue(credential.getCredentialType() == "my-credential" ||
									credential.getCredentialType() == "another-credential",
							"Credential should have correct type");

					List<EndpointEntry> endpointEntries = credential.getTrustedLists();
					assertEquals(3, endpointEntries.size(), "Trusted lists should have 3 entries");

					for (EndpointEntry endpointEntry : endpointEntries) {
						if (endpointEntry.getType() == EndpointType.TRUSTED_ISSUERS) {
							assertEquals("http://til.de", endpointEntry.getEndpoint(),
									"Trusted issuer endpoint should have correct URL");
						} else if (endpointEntry.getType() == EndpointType.TRUSTED_PARTICIPANTS) {
							assertTrue(
									endpointEntry.getEndpoint().equals("http://tir.de") ||
											endpointEntry.getEndpoint().equals("http://another-tir.de"),
									"Trusted participants endpoints should have correct URL");
						} else {
							fail("Invalid EndpointEntry type");
						}
					}
				}
			} else {
				fail("ServiceScope should have either 1 or 2 credentials");
			}
		}
	}

	@Test
	void testMapToServiceVO() {
		// Create Service
		EndpointEntry endpointEntryTil = new EndpointEntry();
		endpointEntryTil.setType(EndpointType.TRUSTED_ISSUERS);
		endpointEntryTil.setEndpoint("http://til.de");
		EndpointEntry endpointEntryTir = new EndpointEntry();
		endpointEntryTir.setType(EndpointType.TRUSTED_PARTICIPANTS);
		endpointEntryTir.setEndpoint("http://tir.de");

		Credential credential = new Credential();
		credential.setCredentialType("my-credential");
		List<EndpointEntry> endpointEntries = List.of(endpointEntryTil, endpointEntryTir);
		credential.setTrustedLists(endpointEntries);

		Collection<Credential> credentials = List.of(credential);

		Service service = new Service();
		service.setDefaultOidcScope("test-oidc-scope");

		ScopeEntry scopeEntry = new ScopeEntry();
		scopeEntry.setCredentials(credentials);
		scopeEntry.setScopeKey("test-oidc-scope");

		service.setOidcScopes(List.of(scopeEntry));

		// Map to ServiceVO
		ServiceVO serviceVO = serviceMapper.map(service);

		// Asserts
		assertEquals("test-oidc-scope", serviceVO.getDefaultOidcScope(),
				"ServiceVO should have correct defaultOidcScope");

		Map<String, ServiceScopesEntryVO> serviceScopes = serviceVO.getOidcScopes();

		assertEquals(1, serviceScopes.size(), "ServiceVO should have 1 OIDC scope");

		ServiceScopesEntryVO serviceScopesEntryVO = serviceScopes.get("test-oidc-scope");
		assertNotNull(serviceScopesEntryVO,
				"ServiceVO should have an OIDC scope with key '\"test-oidc-scope\"'");

		assertEquals(1, serviceScopesEntryVO.getCredentials().size(),
				"the OIDC scope should have 1 credential");
		CredentialVO credentialVO = serviceScopesEntryVO.getCredentials().get(0);
		assertEquals("my-credential", credentialVO.getType(),
				"the credential should have the correct type");

		List<TrustedParticipantsListEndpointVO> trustedParticipantsLists = credentialVO.getTrustedParticipantsLists()
				.stream()
				.filter(TrustedParticipantsListEndpointVO.class::isInstance)
				.map(TrustedParticipantsListEndpointVO.class::cast)
				.toList();
		assertEquals(1, trustedParticipantsLists.size(),
				"the trusted participants list should have 1 entry");
		TrustedParticipantsListEndpointVO trustedParticipantsListVO = trustedParticipantsLists.get(0);
		assertEquals("http://tir.de", trustedParticipantsListVO.getUrl(),
				"the trusted participants list entry should have the correct URL");
		assertEquals(TrustedParticipantsListEndpointVO.Type.EBSI, trustedParticipantsListVO.getType(),
				"the trusted participants list entry should have the correct type");

		List<String> trustedIssuersLists = credentialVO.getTrustedIssuersLists();
		assertEquals(1, trustedIssuersLists.size(),
				"the trusted issuers list should have 1 entry");
		String trustedIssuerUrl = trustedIssuersLists.get(0);
		assertEquals("http://til.de", trustedIssuerUrl,
				"the trusted issuers list entry should have the correct URL");
	}

	@ParameterizedTest
	@MethodSource("serviceAndVo")
	public void testServiceVoToService(ServiceVO serviceVO, Service expectedService) {
		assertEquals(expectedService, serviceMapper.map(serviceVO));
	}

	@ParameterizedTest
	@MethodSource("serviceAndVo")
	public void testServiceToServiceVO(ServiceVO expectedServiceVO, Service service) {
		assertEquals(expectedServiceVO, serviceMapper.map(service));
	}

	public static Stream<Arguments> serviceAndVo() {
		List<Arguments> arguments = new ArrayList<>();

		ServiceVO serviceVO_01 = ServiceVOTestExample.build();
		Service expectedService_01 = new Service();
		expectedService_01.setId("packet-delivery-service");
		expectedService_01.setDefaultOidcScope("default");
		expectedService_01.setOidcScopes(List.of());

		arguments.add(Arguments.of(serviceVO_01, expectedService_01));

		ServiceVO serviceVO_02 = ServiceVOTestExample.build()
				.oidcScopes(Map.of("default", ServiceScopesEntryVOTestExample.build()));

		Service expectedService_02 = new Service();
		expectedService_02.setId("packet-delivery-service");
		expectedService_02.setDefaultOidcScope("default");

		PresentationDefinition presentationDefinition_02 = new PresentationDefinition();
		presentationDefinition_02.setId("32f54163-7166-48f1-93d8-ff217bdb0653");
		presentationDefinition_02.setName("My default service credentials");
		presentationDefinition_02.setPurpose("The service requires age and name of the requesting user.");
		presentationDefinition_02.setInputDescriptors(List.of());

		ScopeEntry scopeEntry_02 = new ScopeEntry();
		scopeEntry_02.setScopeKey("default");
		scopeEntry_02.setCredentials(List.of());
		scopeEntry_02.setPresentationDefinition(presentationDefinition_02);
		expectedService_02.setOidcScopes(List.of(scopeEntry_02));

		arguments.add(Arguments.of(serviceVO_02, expectedService_02));

		ServiceVO serviceVO_03 = ServiceVOTestExample.build()
				.oidcScopes(Map.of(
						"default",
						ServiceScopesEntryVOTestExample
								.build()
								.credentials(List.of(CredentialVOTestExample.build()))));

		Service expectedService_03 = new Service();
		expectedService_03.setId("packet-delivery-service");
		expectedService_03.setDefaultOidcScope("default");

		PresentationDefinition presentationDefinition_03 = new PresentationDefinition();
		presentationDefinition_03.setId("32f54163-7166-48f1-93d8-ff217bdb0653");
		presentationDefinition_03.setName("My default service credentials");
		presentationDefinition_03.setPurpose("The service requires age and name of the requesting user.");
		presentationDefinition_03.setInputDescriptors(List.of());

		Credential credential_03 = new Credential();
		credential_03.setCredentialType("VerifiableCredential");
		credential_03.setVerifyHolder(false);
		credential_03.setHolderClaim("subject");
		credential_03.setTrustedLists(List.of());

		ScopeEntry scopeEntry_03 = new ScopeEntry();
		scopeEntry_03.setScopeKey("default");
		scopeEntry_03.setCredentials(List.of(credential_03));
		scopeEntry_03.setPresentationDefinition(presentationDefinition_03);
		expectedService_03.setOidcScopes(List.of(scopeEntry_03));

		arguments.add(Arguments.of(serviceVO_03, expectedService_03));

		ServiceVO serviceVO_04 = ServiceVOTestExample.build()
				.oidcScopes(Map.of(
						"default",
						ServiceScopesEntryVOTestExample
								.build()
								.credentials(
										List.of(
												CredentialVOTestExample.build()
														.trustedIssuersLists(List.of("http://my-url.org"))
														.trustedParticipantsLists(List.of(TrustedParticipantsListEndpointVOTestExample.build()
																.type(TrustedParticipantsListEndpointVO.Type.GAIA_X)
																.url("http://my-url.org")))))));

		Service expectedService_04 = new Service();
		expectedService_04.setId("packet-delivery-service");
		expectedService_04.setDefaultOidcScope("default");

		PresentationDefinition presentationDefinition_04 = new PresentationDefinition();
		presentationDefinition_04.setId("32f54163-7166-48f1-93d8-ff217bdb0653");
		presentationDefinition_04.setName("My default service credentials");
		presentationDefinition_04.setPurpose("The service requires age and name of the requesting user.");
		presentationDefinition_04.setInputDescriptors(List.of());

		EndpointEntry endpointEntry_04_01 = new EndpointEntry();
		endpointEntry_04_01.setListType(ListType.GAIA_X);
		endpointEntry_04_01.setEndpoint("http://my-url.org");
		endpointEntry_04_01.setType(EndpointType.TRUSTED_PARTICIPANTS);

		EndpointEntry endpointEntry_04_02 = new EndpointEntry();
		endpointEntry_04_02.setEndpoint("http://my-url.org");
		endpointEntry_04_02.setType(EndpointType.TRUSTED_ISSUERS);

		Credential credential_04 = new Credential();
		credential_04.setCredentialType("VerifiableCredential");
		credential_04.setVerifyHolder(false);
		credential_04.setHolderClaim("subject");
		credential_04.setTrustedLists(List.of(
				endpointEntry_04_02,
				endpointEntry_04_01
		));

		ScopeEntry scopeEntry_04 = new ScopeEntry();
		scopeEntry_04.setScopeKey("default");
		scopeEntry_04.setCredentials(List.of(credential_04));
		scopeEntry_04.setPresentationDefinition(presentationDefinition_04);
		expectedService_04.setOidcScopes(List.of(scopeEntry_04));

		arguments.add(Arguments.of(serviceVO_04, expectedService_04));

		ServiceVO serviceVO_05 = ServiceVOTestExample.build()
				.oidcScopes(Map.of("default", ServiceScopesEntryVOTestExample.build()
						.presentationDefinition(PresentationDefinitionVOTestExample.build()
								.inputDescriptors(List.of(InputDescriptorVOTestExample.build()
										.constraints(ConstraintsVOTestExample.build().fields(List.of(FieldVOTestExample.build()))))))));

		Service expectedService_05 = new Service();
		expectedService_05.setId("packet-delivery-service");
		expectedService_05.setDefaultOidcScope("default");

		Field field_05 = new Field();
		field_05.setId("32f54163-7166-48f1-93d8-ff217bdb0653");
		field_05.setName("User Age request");
		field_05.setPurpose("Only users above a certain age should get service access");
		field_05.setOptional(false);
		field_05.setPath(List.of());

		Constraints constraints_05 = new Constraints();
		constraints_05.setFields(List.of(field_05));

		InputDescriptor inputDescriptor_05 = new InputDescriptor();
		inputDescriptor_05.setId("32f54163-7166-48f1-93d8-ff217bdb0653");
		inputDescriptor_05.setName("User Age request");
		inputDescriptor_05.setPurpose("Only users above a certain age should get service access");
		inputDescriptor_05.setConstraints(constraints_05);

		PresentationDefinition presentationDefinition_05 = new PresentationDefinition();
		presentationDefinition_05.setId("32f54163-7166-48f1-93d8-ff217bdb0653");
		presentationDefinition_05.setName("My default service credentials");
		presentationDefinition_05.setPurpose("The service requires age and name of the requesting user.");
		presentationDefinition_05.setInputDescriptors(List.of(inputDescriptor_05));

		ScopeEntry scopeEntry_05 = new ScopeEntry();
		scopeEntry_05.setScopeKey("default");
		scopeEntry_05.setCredentials(List.of());
		scopeEntry_05.setPresentationDefinition(presentationDefinition_05);
		expectedService_05.setOidcScopes(List.of(scopeEntry_05));

		arguments.add(Arguments.of(serviceVO_05, expectedService_05));

		return arguments.stream();
	}

}
