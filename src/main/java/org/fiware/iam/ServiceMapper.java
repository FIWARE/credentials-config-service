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

	Service map(ServiceVO serviceVO);

	TrustedParticipantsListEndpointVO.Type map(ListType type);

	ListType map(TrustedParticipantsListEndpointVO.Type type);

	default Map<String, Collection<Credential>> map(Map<String, ServiceScopesEntryVO> value) {
		return Optional.ofNullable(value)
				.orElseGet(Map::of)
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(this::map).toList()));
	}

	default Map<String, ServiceScopesEntryVO> mapCredentials(Map<String, Collection<Credential>> value) {
		Map<String, ServiceScopesEntryVO> answer = new HashMap<>();
		Optional.ofNullable(value)
				.orElseGet(Map::of)
				.entrySet().forEach(entry -> {
					ServiceScopesEntryVO credentialVOS = new ServiceScopesEntryVO();
					entry.getValue().stream().map(this::map).forEach(credentialVOS::add);
					answer.put(entry.getKey(), credentialVOS);
				});
		return answer;
	}

	ServiceVO map(Service service);

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
		Credential credential = new Credential()
				.setCredentialType(credentialVO.getType());
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
		credential.setIncludeInJwt(credentialVO.getIncludeInJWT());
		return credential;
	}

	default Collection<CredentialVO> map(Collection<Credential> credentials) {
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
				.includeInJWT(credential.isIncludeInJwt())
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
				.map(endpoint -> new EndpointEntry()
						.setEndpoint(endpoint.getUrl())
						.setListType(map(endpoint.getType()))
						.setType(EndpointType.TRUSTED_PARTICIPANTS))
				.toList();
	}

	default EndpointEntry participantToEntry(TrustedParticipantsListEndpointVO trustedParticipantsListVO) {
		return new EndpointEntry()
				.setEndpoint(trustedParticipantsListVO.getUrl())
				.setListType(map(trustedParticipantsListVO.getType()))
				.setType(EndpointType.TRUSTED_PARTICIPANTS);
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
				.map(endpoint -> new EndpointEntry()
						.setEndpoint(endpoint)
						.setType(EndpointType.TRUSTED_ISSUERS))
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