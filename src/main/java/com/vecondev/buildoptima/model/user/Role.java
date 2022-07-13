package com.vecondev.buildoptima.model.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import static com.vecondev.buildoptima.model.user.Authority.RESOURCE_READ;
import static com.vecondev.buildoptima.model.user.Authority.RESOURCE_WRITE;
import static com.vecondev.buildoptima.model.user.Authority.USER_READ;

public enum Role {

    ADMIN(Set.of(USER_READ, RESOURCE_WRITE, RESOURCE_READ)),
    MODERATOR(Set.of(RESOURCE_WRITE, RESOURCE_READ)),
    CLIENT(Set.of());

    private static final String ROLE_PREFIX = "ROLE_";
    private Set<Authority> authorities;

    Role(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public Set<SimpleGrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();

        authorities.forEach(authority -> grantedAuthorities.add(new SimpleGrantedAuthority(authority.name().toLowerCase())));
        grantedAuthorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + this.name()));

        return grantedAuthorities;
    }
}
