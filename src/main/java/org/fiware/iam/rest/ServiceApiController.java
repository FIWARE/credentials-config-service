package org.fiware.iam.rest;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiware.iam.ServiceMapper;
import org.fiware.iam.ccs.api.ServiceApi;
import org.fiware.iam.ccs.model.*;
import org.fiware.iam.exception.ConflictException;
import org.fiware.iam.repository.Service;
import org.fiware.iam.repository.ServiceRepository;

import javax.transaction.Transactional;
import java.net.URI;
import java.util.Optional;

/**
 * Implementation of the service api to configure services and there credentials
 */
@Slf4j
@Controller("${general.basepath:/}")
@RequiredArgsConstructor
@Introspected
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
		log.warn("Mapped service {}", mappedService);
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
		scopeVO.addAll(serviceScopesEntryVO.getCredentials().stream().map(CredentialVO::getType).toList());
		return HttpResponse.ok(scopeVO);
	}

	@Override
	public HttpResponse<ServiceVO> getService(@NonNull String id) {
		Optional<ServiceVO> serviceVO = serviceRepository.findById(id)
				.map(serviceMapper::map);
		log.warn("Got service {}", serviceVO.get());

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
		if (!serviceRepository.existsById(id)) {
			return HttpResponse.notFound();
		}
		// just in case none is set in the object
		serviceVO.setId(id);
		serviceRepository.deleteById(id);
		return HttpResponse.ok(serviceMapper.map(serviceRepository.save(serviceMapper.map(serviceVO))));
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

		Optional<CredentialVO> nullType = serviceScopesEntryVO
				.getCredentials()
				.stream()
				.filter(cvo -> cvo.getType() == null)
				.findFirst();
		if (nullType.isPresent()) {
			throw new IllegalArgumentException("Type of a credential cannot be null.");
		}
	}

}
