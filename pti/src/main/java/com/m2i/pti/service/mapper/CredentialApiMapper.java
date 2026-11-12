package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.CredentialApi;
import com.m2i.pti.service.dto.CredentialApiDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity CredentialApi and its DTO CredentialApiDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface CredentialApiMapper extends EntityMapper<CredentialApiDTO, CredentialApi> {

    //CredentialApiDTO toDto(CredentialApi credentialApi);

    @Mapping(target = "scope", ignore = true)
    CredentialApi toEntity(CredentialApiDTO credentialApiDTO);
    default CredentialApi fromId(Long id) {
        if (id == null) {
            return null;
        }
        CredentialApi credentialApi = new CredentialApi();
        credentialApi.setId(id);
        return credentialApi;
    }
}
