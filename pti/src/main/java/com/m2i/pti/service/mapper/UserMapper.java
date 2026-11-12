package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.Authority;
import com.m2i.pti.domain.PointVente;
import com.m2i.pti.domain.User;
import com.m2i.pti.repository.PointVenteRepository;
import com.m2i.pti.repository.ProfilRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.dto.UserDTO;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper for the entity User and its DTO called UserDTO.
 *
 * Normal mappers are generated using MapStruct, this one is hand-coded as
 * MapStruct support is still in beta, and requires a manual step with an IDE.
 */
@Service
public class UserMapper {

    private final ProfilRepository profilRepository;
    private final PointVenteRepository pointVenteRepository;
    private final UniteOrganisationRepository uniteOrganisationRepository;

    public UserMapper(ProfilRepository profilRepository, UniteOrganisationRepository uniteOrganisationRepository, PointVenteRepository pointVenteRepository) {
        this.profilRepository = profilRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.pointVenteRepository = pointVenteRepository;
    }

    public UserDTO userToUserDTO(User user) {
        return new UserDTO(user);
    }

    public List<UserDTO> usersToUserDTOs(List<User> users) {
        return users.stream()
                .filter(Objects::nonNull)
                .map(this::userToUserDTO)
                .collect(Collectors.toList());
    }

    public User userDTOToUser(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        } else {
            User user = new User();
            user.setId(userDTO.getId());
            user.setLogin(userDTO.getLogin());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setImageUrl(userDTO.getImageUrl());
            user.setActivated(userDTO.isActivated());
            user.setLangKey(userDTO.getLangKey());
            PointVente pv = new PointVente();
            pv.setId(userDTO.getPointVenteId());
            user.setPointVente(pv);
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setProfil(profilRepository.findById(userDTO.getProfilId()).orElse(null));
            user.setUniteOrganisation(uniteOrganisationRepository.findByUoId(userDTO.getUniteOrganisationId()));
            Set<Authority> authorities = this.authoritiesFromStrings(userDTO.getAuthorities());
            if (authorities != null) {
                user.setAuthorities(authorities);
            } else {
                Set<String> aut =  profilRepository.findRoleByProfilId(userDTO.getProfilId());
                authorities = this.authoritiesFromStrings(aut);
                if (authorities != null) {
                    user.setAuthorities(authorities);
                }
            }
            return user;
        }
    }

    public List<User> userDTOsToUsers(List<UserDTO> userDTOs) {
        return userDTOs.stream()
                .filter(Objects::nonNull)
                .map(this::userDTOToUser)
                .collect(Collectors.toList());
    }

    public User userFromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

    public Set<Authority> authoritiesFromStrings(Set<String> strings) {
        if (strings != null && !strings.isEmpty()) {
            return strings.stream().map(string -> {
                Authority auth = new Authority();
                auth.setName(string);
                return auth;
            }).collect(Collectors.toSet());
        } else {
            return null;
        }

    }
}
