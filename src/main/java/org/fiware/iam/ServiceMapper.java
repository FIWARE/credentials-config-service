package org.fiware.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wistefan.dcql.model.ClaimsQuery;
import io.github.wistefan.dcql.model.CredentialFormat;
import io.github.wistefan.dcql.model.CredentialQuery;
import io.github.wistefan.dcql.model.CredentialSetQuery;
import io.github.wistefan.dcql.model.DcqlQuery;
import io.github.wistefan.dcql.model.TrustedAuthorityQuery;
import io.github.wistefan.dcql.model.TrustedAuthorityType;
import org.fiware.iam.ccs.model.ClaimsQueryVO;
import org.fiware.iam.ccs.model.CredentialQueryVO;
import org.fiware.iam.ccs.model.CredentialSetQueryVO;
import org.fiware.iam.ccs.model.CredentialVO;
import org.fiware.iam.ccs.model.DCQLVO;
import org.fiware.iam.ccs.model.HolderVerificationVO;
import org.fiware.iam.ccs.model.InputDescriptorVO;
import org.fiware.iam.ccs.model.JwtInclusionVO;
import org.fiware.iam.ccs.model.MetaDataQueryVO;
import org.fiware.iam.ccs.model.PresentationDefinitionVO;
import org.fiware.iam.ccs.model.ServiceScopesEntryVO;
import org.fiware.iam.ccs.model.ServiceVO;
import org.fiware.iam.ccs.model.TrustedAuthorityQueryVO;
import org.fiware.iam.ccs.model.TrustedParticipantsListEndpointVO;
import org.fiware.iam.repository.AuthorizationType;
import org.fiware.iam.repository.Credential;
import org.fiware.iam.repository.EndpointEntry;
import org.fiware.iam.repository.EndpointType;
import org.fiware.iam.repository.Format;
import org.fiware.iam.repository.FormatObject;
import org.fiware.iam.repository.InputDescriptor;
import org.fiware.iam.repository.JwtInclusion;
import org.fiware.iam.repository.ListType;
import org.fiware.iam.repository.PresentationDefinition;
import org.fiware.iam.repository.ScopeEntry;
import org.fiware.iam.repository.Service;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Responsible for mapping entities from the Service api domain to the internal model.
 */
@Mapper(componentModel = "jsr330")
public interface ServiceMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    String JSON_PROPERTY_VCT_VALUES = "vct_values";
    String JSON_PROPERTY_DOCTYPE_VALUE = "doctype_value";
    String JSON_PROPERTY_TYPE_VALUES = "type_values";

    default Service map(ServiceVO serviceVO) {
        if (serviceVO == null) {
            return null;
        }
        Service service = new Service();
        service.setId(serviceVO.getId());
        service.setAuthorizationType(map(serviceVO.getAuthorizationType()));
        service.setDefaultOidcScope(serviceVO.getDefaultOidcScope());
        service.setOidcScopes(
                serviceVO.getOidcScopes()
                        .entrySet()
                        .stream()
                        .map(e -> {
                            ScopeEntry scopeEntry = new ScopeEntry();
                            scopeEntry.setScopeKey(e.getKey());
                            scopeEntry.setCredentials(Optional.ofNullable(e.getValue().getCredentials()).
                                    orElse(List.of()).stream().map(this::map).toList());
                            scopeEntry.setPresentationDefinition(this.map(e.getValue().getPresentationDefinition()));
                            scopeEntry.setDcqlQuery(this.map(e.getValue().getDcql()));
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
                .authorizationType(map(service.getAuthorizationType()))
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

    default Collection<Format> mapFormatVO(Map<String, Object> formatsMap) {
        if (formatsMap == null) {
            return null;
        }

        return formatsMap
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

    default Map<String, Object> mapFormats(Collection<Format> formats) {
        if (formats == null) {
            return null;
        }

        Map<String, Object> formatsMap = new HashMap<>();

        formats.forEach(format -> {
            Map<String, Object> formatMap = new HashMap<>();
            Optional.ofNullable(format.getAlg()).ifPresent(algs -> formatMap.put("alg", algs));
            Optional.ofNullable(format.getProofType()).ifPresent(pt -> formatMap.put("proof_type", pt));
            formatsMap.put(format.getFormatKey(), formatMap);
        });

        return formatsMap;
    }

    AuthorizationType map(ServiceVO.AuthorizationType authorizationType);

    ServiceVO.AuthorizationType map(AuthorizationType authorizationType);

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
                    scopesEntryVO.setDcql(this.map(entry.getDcqlQuery()));
                    scopes.put(entry.getScopeKey(), scopesEntryVO);
                });
        return scopes;
    }

    DCQLVO map(DcqlQuery dcqlQuery);

    DcqlQuery map(DCQLVO dcqlvo);

    default CredentialQuery map(CredentialQueryVO credentialQueryVO) {
        if (credentialQueryVO == null) {
            return null;
        }
        CredentialQuery credentialQuery = new CredentialQuery();
        credentialQuery.setId(credentialQueryVO.getId());
        credentialQuery.setFormat(this.map(credentialQueryVO.getFormat()));
        credentialQuery.setMultiple(credentialQueryVO.getMultiple());
        credentialQuery.setClaims(Optional.ofNullable(credentialQueryVO.getClaims()).orElse(List.of())
                .stream().map(this::map).toList());
        credentialQuery.setMeta(this.map(credentialQueryVO.getMeta()));
        credentialQuery.setRequireCryptographicHolderBinding(credentialQueryVO.getRequireCryptographicHolderBinding());
        credentialQuery.setClaimSets(credentialQueryVO.getClaimSets());
        credentialQuery.setTrustedAuthorities(Optional.ofNullable(credentialQueryVO.getTrustedAuthorities()).orElse(List.of())
                .stream().map(this::map).toList());
        return credentialQuery;
    }

    default CredentialQueryVO map(CredentialQuery credentialQuery) {
        if (credentialQuery == null) {
            return null;
        }
        CredentialQueryVO credentialQueryVO = new CredentialQueryVO();
        credentialQueryVO.setId(credentialQuery.getId());
        credentialQueryVO.setFormat(this.map(credentialQuery.getFormat()));
        credentialQueryVO.setMultiple(credentialQuery.getMultiple());
        credentialQueryVO.setClaims(Optional.ofNullable(credentialQuery.getClaims()).orElse(List.of())
                .stream().map(this::map).toList());
        credentialQueryVO.setMeta(map(credentialQuery.getMeta()));
        credentialQueryVO.setRequireCryptographicHolderBinding(credentialQuery.getRequireCryptographicHolderBinding());
        credentialQueryVO.setClaimSets(credentialQuery.getClaimSets());
        credentialQueryVO.setTrustedAuthorities(Optional.ofNullable(credentialQuery.getTrustedAuthorities()).orElse(List.of())
                .stream().map(this::map).toList());
        return credentialQueryVO;
    }

    TrustedAuthorityQueryVO map(TrustedAuthorityQuery trustedAuthorityQuery);

    TrustedAuthorityQuery map(TrustedAuthorityQueryVO trustedAuthorityQueryVO);

    default TrustedAuthorityType map(String type) {
        return TrustedAuthorityType.fromValue(type);
    }

    default String map(TrustedAuthorityType type) {
        return type.getValue();
    }

    default Map<String, Object> map(MetaDataQueryVO metaDataQueryVO) {
        if (metaDataQueryVO == null) {
            return null;
        }
        Map<String, Object> metaMap = new HashMap<>();
        if (metaDataQueryVO.getVctValues() != null) {
            metaMap.put(JSON_PROPERTY_VCT_VALUES, metaDataQueryVO.getVctValues());
        }
        if (metaDataQueryVO.getTypeValues() != null) {
            metaMap.put(JSON_PROPERTY_TYPE_VALUES, metaDataQueryVO.getTypeValues());
        }
        if (metaDataQueryVO.getDoctypeValue() != null) {
            metaMap.put(JSON_PROPERTY_DOCTYPE_VALUE, metaDataQueryVO.getDoctypeValue());
        }
        return metaMap;
    }

    default MetaDataQueryVO map(Map<String, Object> meta) {
        if (meta == null) {
            return null;
        }
        MetaDataQueryVO metaDataQueryVO = new MetaDataQueryVO();
        if (meta.containsKey(JSON_PROPERTY_VCT_VALUES)) {
            metaDataQueryVO.setVctValues((List<String>) meta.get(JSON_PROPERTY_VCT_VALUES));
        }
        if (meta.containsKey(JSON_PROPERTY_DOCTYPE_VALUE)) {
            metaDataQueryVO.setDoctypeValue((String) meta.get(JSON_PROPERTY_DOCTYPE_VALUE));
        }
        if (meta.containsKey(JSON_PROPERTY_TYPE_VALUES)) {
            metaDataQueryVO.setTypeValues((List<List<String>>) meta.get(JSON_PROPERTY_TYPE_VALUES));
        }
        return metaDataQueryVO;
    }

    ClaimsQuery map(ClaimsQueryVO claimsQueryVO);

    ClaimsQueryVO map(ClaimsQuery claimsQuery);

    CredentialQueryVO.Format map(CredentialFormat credentialFormat);

    CredentialFormat map(CredentialQueryVO.Format credentialFormat);

    CredentialSetQueryVO map(CredentialSetQuery credentialSetQuery);

    CredentialSetQuery map(CredentialSetQueryVO credentialSetQueryVO);

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