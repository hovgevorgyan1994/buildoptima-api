package com.vecondev.buildoptima.model.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import static com.vecondev.buildoptima.model.user.Authority.RESOURCE_WRITE;

public enum Role {
  ADMIN(Set.of(RESOURCE_WRITE)),
  MODERATOR(Set.of(RESOURCE_WRITE)),
  CLIENT(Set.of());

  private static final String ROLE_PREFIX = "ROLE_";
  private final Set<Authority> authorities;

  Role(Set<Authority> authorities) {
    this.authorities = authorities;
  }

  public Set<SimpleGrantedAuthority> getAuthorities() {
    Set<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();

    authorities.forEach(
        authority ->
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.name().toLowerCase())));
    grantedAuthorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + this.name()));

    return grantedAuthorities;
  }
}
