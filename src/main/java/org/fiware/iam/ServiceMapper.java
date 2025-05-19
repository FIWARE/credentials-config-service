package org.fiware.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fiware.iam.ccs.model.*;
import org.fiware.iam.repository.*;
import org.mapstruct.Mapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for mapping entities from the Service api domain to the internal model.
 */
@Mapper(componentModel = "jsr330")
public interface ServiceMapper {

	static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	default Service map(ServiceVO serviceVO) {
		if (serviceVO == null) {
			return null;
		}
		Service service = new Service();
		service.setId(serviceVO.getId());
		service.setDefaultOidcScope(serviceVO.getDefaultOidcScope());
		service.setOidcScopes(
				serviceVO.getOidcScopes()
						.entrySet()
						.stream()
						.map(e -> {
							ScopeEntry scopeEntry = new ScopeEntry();
							scopeEntry.setScopeKey(e.getKey());
							scopeEntry.setCredentials(e.getValue().getCredentials().stream().map(this::map).toList());
							scopeEntry.setPresentationDefinition(this.map(e.getValue().getPresentationDefinition()));
							return scopeEntry;
						})
						.toList());
		return service;
	}

	default ServiceVO map(Service service) {
		if (service == null) {
			return null;
		}
		return new ServiceVO().id(service.getId())
				.defaultOidcScope(service.getDefaultOidcScope())
				.oidcScopes(this.toOidcScopes(service.getOidcScopes()));
	}

	default PresentationDefinition map(PresentationDefinitionVO presentationDefinitionVO) {
		if (presentationDefinitionVO == null) {
			return null;
		}

		PresentationDefinition presentationDefinition = new PresentationDefinition();
		presentationDefinition.setId(presentationDefinitionVO.getId());
		presentationDefinition.setName(presentationDefinitionVO.getName());
		presentationDefinition.setPurpose(presentationDefinitionVO.getPurpose());
		presentationDefinition.setInputDescriptors(this.mapInputDescriptors(presentationDefinitionVO
				.getInputDescriptors()));
		presentationDefinition.setFormat(this.mapFormatVO(presentationDefinitionVO
				.getFormat()));

		return presentationDefinition;
	}

	default Collection<InputDescriptor> mapInputDescriptors(Collection<InputDescriptorVO> inputDescriptorVOS) {
		if (inputDescriptorVOS == null) {
			return null;
		}
		return inputDescriptorVOS
				.stream()
				.map(this::mapInputDescriptorVO).toList();
	}

	default Collection<Format> mapFormatVO(FormatVO formatVO) {
		if (formatVO == null || formatVO.getAdditionalProperties() == null) {
			return null;
		}

		return formatVO.getAdditionalProperties()
				.entrySet()
				.stream()
				.map(e -> {
					FormatObject formatObject = OBJECT_MAPPER.convertValue(e.getValue(), FormatObject.class);
					Format format = new Format();
					format.setFormatKey(e.getKey());
					format.setAlg(formatObject.getAlg());
					format.setProofType(formatObject.getProofType());
					return format;
				}).toList();
	}

	default FormatVO mapFormats(Collection<Format> formats) {
		if (formats == null) {
			return null;
		}

		FormatVO formatVO = new FormatVO();
		formats
				.forEach(f -> {
					FormatObject formatObject = new FormatObject();
					formatObject.setAlg(f.getAlg());
					formatObject.setProofType(f.getProofType());
					formatVO.setAdditionalProperties(f.getFormatKey(), formatObject);
				});
		return formatVO;
	}

	InputDescriptorVO mapInputDescriptor(InputDescriptor inputDescriptor);

	InputDescriptor mapInputDescriptorVO(InputDescriptorVO inputDescriptor);

	PresentationDefinitionVO map(PresentationDefinition presentationDefinition);

	TrustedParticipantsListEndpointVO.Type map(ListType type);

	ListType map(TrustedParticipantsListEndpointVO.Type type);

	default Map<String, ServiceScopesEntryVO> toOidcScopes(Collection<ScopeEntry> scopeEntries) {
		if (scopeEntries == null) {
			return null;
		}
		Map<String, ServiceScopesEntryVO> scopes = new LinkedHashMap<>();
		scopeEntries
				.forEach(entry -> {
					ServiceScopesEntryVO scopesEntryVO = new ServiceScopesEntryVO();
					scopesEntryVO.setPresentationDefinition(this.map(entry.getPresentationDefinition()));
					scopesEntryVO.setCredentials(this.map(entry.getCredentials()));
					scopes.put(entry.getScopeKey(), scopesEntryVO);
				});
		return scopes;
	}

	default EndpointEntry stringToEndpointEntry(String url) {
		EndpointEntry entry = new EndpointEntry();
		entry.setType(EndpointType.TRUSTED_PARTICIPANTS);
		entry.setListType(ListType.EBSI);
		entry.setEndpoint(url);
		return entry;
	}

	default Credential map(CredentialVO credentialVO) {
		if (credentialVO == null) {
			return null;
		}
		Credential credential = new Credential();
		credential.setCredentialType(credentialVO.getType());
		List<EndpointEntry> trustedList = new ArrayList<>();
		Optional.ofNullable(issuersToEntries(credentialVO.getTrustedIssuersLists())).ifPresent(trustedList::addAll);

		credentialVO.getTrustedParticipantsLists()
				.forEach(tpl -> {
					if (tpl instanceof String tplS) {
						trustedList.add(stringToEndpointEntry(tplS));
					} else {
						trustedList.add(participantToEntry(OBJECT_MAPPER.convertValue(tpl, TrustedParticipantsListEndpointVO.class)));
					}
				});

		credential.setTrustedLists(trustedList);

		if (credentialVO.getHolderVerification() != null) {
			credential.setHolderClaim(credentialVO.getHolderVerification().getClaim());
			credential.setVerifyHolder(credentialVO.getHolderVerification().getEnabled());
		} else {
			credential.setVerifyHolder(false);
			credential.setHolderClaim(null);
		}
		credential.setRequireCompliance(credentialVO.getRequireCompliance());
		credential.setJwtInclusion(map(credentialVO.getJwtInclusion()));
		return credential;
	}

	JwtInclusion map(JwtInclusionVO jwtInclusionVO);
	JwtInclusionVO map(JwtInclusion jwtInclusion);

	default List<CredentialVO> map(Collection<Credential> credentials) {
		if (credentials == null) {
			return null;
		}
		return credentials.stream().map(this::map).toList();
	}

	default CredentialVO map(Credential credential) {
		if (credential == null) {
			return null;
		}
		return new CredentialVO()
				.type(credential.getCredentialType())
				.trustedIssuersLists(entriesToIssuers(credential.getTrustedLists()))
				.trustedParticipantsLists(entriesToParticipants(credential.getTrustedLists()).stream().map(Object.class::cast).toList())
				.requireCompliance(credential.isRequireCompliance())
				.jwtInclusion(map(credential.getJwtInclusion()))
				.holderVerification(new HolderVerificationVO()
						.enabled(credential.isVerifyHolder())
						.claim(credential.getHolderClaim()));
	}

	/**
	 * Map a list of TrustedParticipantsListVO-entries, to a list of {@link EndpointEntry} with
	 * type {{@link EndpointType#TRUSTED_PARTICIPANTS}
	 */
	default List<EndpointEntry> participantsToEntries(List<TrustedParticipantsListEndpointVO> endpoints) {
		if (endpoints == null) {
			return null;
		}
		return endpoints.stream()
				.map(endpoint -> {
					EndpointEntry entry = new EndpointEntry();
					entry.setEndpoint(endpoint.getUrl());
					entry.setListType(map(endpoint.getType()));
					entry.setType(EndpointType.TRUSTED_PARTICIPANTS);
					return entry;
				})
				.toList();
	}

	default EndpointEntry participantToEntry(TrustedParticipantsListEndpointVO trustedParticipantsListVO) {
		if (trustedParticipantsListVO == null) {
			return null;
		}
		EndpointEntry entry = new EndpointEntry();
		entry.setEndpoint(trustedParticipantsListVO.getUrl());
		entry.setListType(map(trustedParticipantsListVO.getType()));
		entry.setType(EndpointType.TRUSTED_PARTICIPANTS);
		return entry;
	}

	/**
	 * Map a list of string-entries, encoding TrustedIssuers endpoints to a list of {@link EndpointEntry} with
	 * type {{@link EndpointType#TRUSTED_ISSUERS}
	 */
	default List<EndpointEntry> issuersToEntries(List<String> endpoints) {
		if (endpoints == null) {
			return null;
		}
		return endpoints.stream()
				.map(endpoint -> {
					EndpointEntry entry = new EndpointEntry();
					entry.setEndpoint(endpoint);
					entry.setType(EndpointType.TRUSTED_ISSUERS);
					return entry;
				})
				.toList();
	}

	/**
	 * Return issuer endpoints from the {@link EndpointEntry} list to a list of strings
	 */
	default List<String> entriesToIssuers(List<EndpointEntry> endpoints) {
		if (endpoints == null) {
			return List.of();
		}
		return endpoints.stream()
				.filter(entry -> entry.getType().equals(EndpointType.TRUSTED_ISSUERS))
				.map(EndpointEntry::getEndpoint)
				.toList();
	}

	/**
	 * Return participant endpoints from the {@link EndpointEntry} list to a list of strings
	 */
	default List<TrustedParticipantsListEndpointVO> entriesToParticipants(List<EndpointEntry> endpoints) {
		if (endpoints == null) {
			return List.of();
		}
		return endpoints.stream()
				.filter(entry -> entry.getType().equals(EndpointType.TRUSTED_PARTICIPANTS))
				.map(entry -> new TrustedParticipantsListEndpointVO()
						.type(map(entry.getListType()))
						.url(entry.getEndpoint()))
				.toList();
	}

}