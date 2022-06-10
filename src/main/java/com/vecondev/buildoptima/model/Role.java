package com.vecondev.buildoptima.model.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

import static com.vecondev.buildoptima.model.user.Authority.USER_READ;

public enum Role {
  ADMIN(Set.of(USER_READ));

  private static final String ROLE_PREFIX = "ROLE_";
  private Set<com.vecondev.buildoptima.model.user.Authority> authorities;

  Role(Set<com.vecondev.buildoptima.model.user.Authority> authorities) {
    this.authorities = authorities;
  }

  public Set<SimpleGrantedAuthority> getAuthorities() {
    Set<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();

    authorities.forEach(
        authority -> grantedAuthorities.add(new SimpleGrantedAuthority(authority.name())));
    grantedAuthorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + this.name()));

    return grantedAuthorities;
  }
}
