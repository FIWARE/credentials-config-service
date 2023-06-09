package org.fiware.iam.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.RequiredArgsConstructor;
import org.fiware.iam.ccs.api.ServiceApiTestClient;
import org.fiware.iam.ccs.api.ServiceApiTestSpec;
import org.fiware.iam.ccs.model.CredentialVOTestExample;
import org.fiware.iam.ccs.model.ScopeVO;
import org.fiware.iam.ccs.model.ServiceVO;
import org.fiware.iam.ccs.model.ServiceVOTestExample;
import org.fiware.iam.ccs.model.ServicesVO;
import org.fiware.iam.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@RequiredArgsConstructor
@MicronautTest
public class ServiceApiControllerTest implements ServiceApiTestSpec {

	public final ServiceApiTestClient testClient;
	public final ServiceRepository serviceRepository;

	private ServiceVO theService;
	private List<String> expectedScopes;
	private int pageSize;
	private int pageNumber;

	@BeforeEach
	public void cleanUp() {
		serviceRepository.deleteAll();
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

	private static Stream<Arguments> validServices() {
		return Stream.of(
				Arguments.of(
						ServiceVOTestExample.build(), List.of()),
				Arguments.of(ServiceVOTestExample.build().id("my-service"), List.of()),
				Arguments.of(ServiceVOTestExample.build().credentials(List.of(CredentialVOTestExample.build())),
						List.of("VerifiableCredential")),
				Arguments.of(ServiceVOTestExample.build()
								.credentials(List.of(CredentialVOTestExample.build().type("my-credential"))),
						List.of("my-credential")),
				Arguments.of(ServiceVOTestExample.build()
								.credentials(List.of(CredentialVOTestExample.build().type("my-credential")
										.trustedIssuersLists(List.of("http://til.de")))
								),
						List.of("my-credential")),
				Arguments.of(ServiceVOTestExample.build()
								.credentials(List.of(CredentialVOTestExample.build().type("my-credential")
										.trustedIssuersLists(List.of("http://til.de", "http://another-til.de")))
								),
						List.of("my-credential")),
				Arguments.of(ServiceVOTestExample.build()
								.credentials(List.of(CredentialVOTestExample.build().type("my-credential")
										.trustedIssuersLists(List.of("http://til.de"))
										.trustedParticipantsLists(List.of("http://tir.de")))
								),
						List.of("my-credential")),
				Arguments.of(ServiceVOTestExample.build()
								.credentials(List.of(CredentialVOTestExample.build().type("my-credential")
										.trustedIssuersLists(List.of("http://til.de"))
										.trustedParticipantsLists(List.of("http://tir.de", "another-tir.de")))
								),
						List.of("my-credential")),
				Arguments.of(ServiceVOTestExample.build()
								.credentials(List.of(CredentialVOTestExample.build().type("my-credential")
										.trustedParticipantsLists(List.of("http://tir.de", "another-tir.de")))
								),
						List.of("my-credential")),
				Arguments.of(ServiceVOTestExample.build()
								.credentials(List.of(
										CredentialVOTestExample.build().type("my-credential")
												.trustedParticipantsLists(List.of("http://tir.de", "another-tir.de")),
										CredentialVOTestExample.build().type("another-credential")
												.trustedIssuersLists(List.of("til.de"))
												.trustedParticipantsLists(List.of("tir.de")))),
						List.of("my-credential", "another-credential")));
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
		return Stream.of(
				Arguments.of(ServiceVOTestExample.build().credentials(null)),
				Arguments.of(ServiceVOTestExample.build().id("my-service").credentials(null)),
				Arguments.of(
						ServiceVOTestExample.build().credentials(List.of(CredentialVOTestExample.build().type(null)))),
				Arguments.of(
						ServiceVOTestExample.build().credentials(
								List.of(CredentialVOTestExample.build(), CredentialVOTestExample.build().type(null))))
		);
	}

	@Test
	@Override
	public void createService409() throws Exception {
		ServiceVO serviceToBeCreated = ServiceVOTestExample.build().id("my-service");
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
	@Override public void deleteServiceById204() throws Exception {
		ServiceVO serviceToBeCreated = ServiceVOTestExample.build().id("my-service");
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
		HttpResponse<ScopeVO> scopeResponse = testClient.getScopeForService("my-service");
		ScopeVO returnedScope = scopeResponse.body();
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
		assertEquals(HttpStatus.NOT_FOUND, testClient.getScopeForService("my-service").getStatus(),
				"If no such service exists, a 404 should be returned.");
	}

	@Override
	public void getService200() throws Exception {

		HttpResponse<ServiceVO> theServiceResponse = testClient.getService(theService.getId());
		assertEquals(HttpStatus.OK, theServiceResponse.getStatus(), "The service should be responded.");
		assertEquals(theService, theServiceResponse.body(), "The service should be responded.");
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
			ServiceVO serviceVO = ServiceVOTestExample.build().id(String.valueOf(i))
					.credentials(List.of(CredentialVOTestExample.build()));
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
		assertEquals(HttpStatus.OK, updatedService.status(), "The service should have been updated.");
		assertEquals(theService, updatedService.body(), "The service should have been updated.");
	}

	@ParameterizedTest
	@MethodSource("validServices")
	public void updateService200(ServiceVO serviceVO) throws Exception {
		HttpResponse<?> initialCreate = testClient.createService(ServiceVOTestExample.build());
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
		HttpResponse<?> initialCreate = testClient.createService(ServiceVOTestExample.build());
		assertEquals(HttpStatus.CREATED, initialCreate.status(), "The creation should have been succeeded.");
		theService = serviceVO;
		updateService400();
	}

	@Test
	@Override
	public void updateService404() throws Exception {
		ServiceVO serviceVO = ServiceVOTestExample.build();
		assertEquals(HttpStatus.NOT_FOUND, testClient.updateService(serviceVO.getId(), serviceVO).status(),
				"Only existing services can be updated.");
	}
}