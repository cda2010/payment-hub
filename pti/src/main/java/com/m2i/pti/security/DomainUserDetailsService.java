package com.m2i.pti.security;

import com.m2i.pti.certificat.CertReader;
import com.m2i.pti.certificat.Encryption;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.User;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.repository.UserRepository;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.DeviseService;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.UserService;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.PrivateKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 */

public class DomainUserDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepository userRepository;

    private final DeviseService deviseService;

    private final Encryption encryption;

    private final CertReader certReader;

    public DomainUserDetailsService(UserRepository userRepository, DeviseService deviseService, Encryption encryption, CertReader certReader) {
        this.userRepository = userRepository;
        this.deviseService = deviseService;
        this.encryption = encryption;
        this.certReader = certReader;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        Optional<User> userFromDatabase = userRepository.findOneWithAuthoritiesByLogin(lowercaseLogin);
        return userFromDatabase.map(user -> {
            if (!user.getActivated()) {
                throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
            }
            List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                    .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                    .collect(Collectors.toList());

            return new org.springframework.security.core.userdetails.User(lowercaseLogin,
                    user.getPassword(),
                    grantedAuthorities);
        }).orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the " +
                "database"));
    }
}
