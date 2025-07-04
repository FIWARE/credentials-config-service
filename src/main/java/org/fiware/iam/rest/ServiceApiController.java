package org.fiware.iam.rest;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiware.iam.ServiceMapper;
import org.fiware.iam.ccs.api.ServiceApi;
import org.fiware.iam.ccs.model.*;
import org.fiware.iam.exception.ConflictException;
import org.fiware.iam.repository.ScopeEntry;
import org.fiware.iam.repository.Service;
import org.fiware.iam.repository.ServiceRepository;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the service api to configure services and there credentials
 */
@Slf4j
@Controller("${general.basepath:/}")
@RequiredArgsConstructor
public class ServiceApiController implements ServiceApi {

	private final ServiceRepository serviceRepository;
	private final ServiceMapper serviceMapper;

	@Override
	public HttpResponse<Object> createService(@NonNull ServiceVO serviceVO) {
		if (serviceVO.getId() != null && serviceRepository.existsById(serviceVO.getId())) {
			throw new ConflictException(String.format("The service with id %s already exists.", serviceVO.getId()),
					serviceVO.getId());
		}
		validateServiceVO(serviceVO);

		Service mappedService = serviceMapper.map(serviceVO);

		Service savedService = serviceRepository.save(mappedService);

		return HttpResponse.created(
				URI.create(
						ServiceApi.PATH_GET_SERVICE.replace(
								"{id}", savedService.getId())));
	}

	@Override
	public HttpResponse<Object> deleteServiceById(@NonNull String id) {
		if (!serviceRepository.existsById(id)) {
			return HttpResponse.notFound();
		}
		serviceRepository.deleteById(id);
		return HttpResponse.noContent();
	}

	@Override
	public HttpResponse<ScopeVO> getScopeForService(@NonNull String id, @Nullable String oidcScope) {
		Optional<Service> service = serviceRepository.findById(id);
		if (service.isEmpty()) {
			return HttpResponse.notFound();
		}
		String selectedOidcScope =
				oidcScope == null ?
						serviceMapper.map(service.get()).getDefaultOidcScope() :
						oidcScope;
		ServiceScopesEntryVO serviceScopesEntryVO = serviceMapper.map(service.get())
				.getOidcScopes()
				.get(selectedOidcScope);
		ScopeVO scopeVO = new ScopeVO();
		scopeVO.addAll(Optional.ofNullable(serviceScopesEntryVO.getCredentials()).orElse(List.of()).stream().map(CredentialVO::getType).toList());
		return HttpResponse.ok(scopeVO);
	}

	@Override
	public HttpResponse<ServiceVO> getService(@NonNull String id) {
		Optional<ServiceVO> serviceVO = serviceRepository.findById(id)
				.map(serviceMapper::map);
		return serviceVO
				.map(HttpResponse::ok)
				.orElse(HttpResponse.notFound());
	}

	@Override
	public HttpResponse<ServicesVO> getServices(@Nullable Integer nullablePageSize,
												@Nullable Integer nullablePage) {
		var pageSize = Optional.ofNullable(nullablePageSize).orElse(100);
		var page = Optional.ofNullable(nullablePage).orElse(0);
		if (pageSize < 1) {
			throw new IllegalArgumentException("PageSize has to be at least 1.");
		}
		if (page < 0) {
			throw new IllegalArgumentException("Offsets below 0 are not supported.");
		}

		Page<Service> requestedPage = serviceRepository.findAll(
				Pageable.from(page, pageSize, Sort.of(Sort.Order.asc("id"))));
		return HttpResponse.ok(
				new ServicesVO()
						.total((int) requestedPage.getTotalSize())
						.pageNumber(page)
						.pageSize(requestedPage.getContent().size())
						.services(requestedPage.getContent().stream().map(serviceMapper::map).toList()));
	}

	@Transactional
	@Override
	public HttpResponse<ServiceVO> updateService(@NonNull String id, @NonNull ServiceVO serviceVO) {
		if (serviceVO.getId() != null && !id.equals(serviceVO.getId())) {
			throw new IllegalArgumentException("The id of a service cannot be updated.");
		}
		validateServiceVO(serviceVO);
		Optional<Service> optionalOrginalService = serviceRepository.findById(id);
		if (optionalOrginalService.isEmpty()) {
			return HttpResponse.notFound();
		}

		Service originalService = optionalOrginalService.get();
		Service toBeUpdated = serviceMapper.map(serviceVO);
		Map<String, ScopeEntry> scopeEntryMap = originalService.getOidcScopes()
				.stream().collect(Collectors.toMap(ScopeEntry::getScopeKey, sE -> sE));

		List<ScopeEntry> scopeEntries = toBeUpdated.getOidcScopes().stream()
				.map(scopeEntry -> {
					if (scopeEntryMap.containsKey(scopeEntry.getScopeKey())) {
						Long originalId = scopeEntryMap.get(scopeEntry.getScopeKey()).getId();
						scopeEntry.setId(originalId);
					}
					return scopeEntry;
				}).toList();

		// just in case none is set in the object
		toBeUpdated.setId(id);
		toBeUpdated.setOidcScopes(scopeEntries);

		Service updatedService = serviceRepository.update(toBeUpdated);
		return HttpResponse.ok(serviceMapper.map(updatedService));
	}

	// validate a service vo, e.g. check forbidden null values
	private void validateServiceVO(ServiceVO serviceVO) {
		if (serviceVO.getDefaultOidcScope() == null) {
			throw new IllegalArgumentException("Default OIDC scope cannot be null.");
		}
		if (serviceVO.getOidcScopes() == null) {
			throw new IllegalArgumentException("OIDC scopes cannot be null.");
		}

		String defaultOidcScope = serviceVO.getDefaultOidcScope();
		ServiceScopesEntryVO serviceScopesEntryVO = serviceVO
				.getOidcScopes()
				.get(defaultOidcScope);
		if (serviceScopesEntryVO == null) {
			throw new IllegalArgumentException("Default OIDC scope must exist in OIDC scopes array.");
		}

		Optional<CredentialVO> nullType = Optional.ofNullable(serviceScopesEntryVO
						.getCredentials())
				.orElse(List.of())
				.stream()
				.filter(cvo -> cvo.getType() == null)
				.findFirst();
		if (nullType.isPresent()) {
			throw new IllegalArgumentException("Type of a credential cannot be null.");
		}

		serviceVO
				.getOidcScopes()
				.values()
				.forEach(this::validateKeyMappings);
	}

	private void validateKeyMappings(ServiceScopesEntryVO scopeEntry) {
		if (scopeEntry.getFlatClaims()) {
			List<String> includedKeys = Optional.ofNullable(scopeEntry.getCredentials())
					.orElse(List.of())
					.stream()
					.filter(cvo -> cvo.getJwtInclusion().getEnabled())
					.flatMap(credentialVO ->
							Optional.ofNullable(credentialVO.getJwtInclusion()
											.getClaimsToInclude())
									.orElse(List.of())
									.stream()
									.map(claim -> {
										if (claim.getNewKey() != null && !claim.getNewKey().isEmpty()) {
											return claim.getNewKey();
										}
										return claim.getOriginalKey();
									})
					).toList();
			if (includedKeys.size() != new HashSet(includedKeys).size()) {
				throw new IllegalArgumentException("Configuration contains duplicate claim keys.");
			}
		} else {
			Optional.ofNullable(scopeEntry.getCredentials())
					.orElse(List.of())
					.stream()
					.filter(cvo -> cvo.getJwtInclusion().getEnabled())
					.forEach(cvo -> {
						List<String> claimKeys = Optional.ofNullable(cvo.getJwtInclusion()
										.getClaimsToInclude())
								.orElse(List.of())
								.stream()
								.map(claim -> {
									if (claim.getNewKey() != null && !claim.getNewKey().isEmpty()) {
										return claim.getNewKey();
									}
									return claim.getOriginalKey();
								})
								.toList();
						if (claimKeys.size() != new HashSet(claimKeys).size()) {
							throw new IllegalArgumentException("Configuration contains duplicate claim keys.");
						}
					});
		}
	}
}
