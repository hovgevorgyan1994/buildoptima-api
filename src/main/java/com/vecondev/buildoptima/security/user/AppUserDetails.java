package com.vecondev.buildoptima.security.user;

import com.vecondev.buildoptima.model.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Getter
public class AppUserDetails implements UserDetails {

    private final UUID id;

    private final String username;
    private final String password;
    private final Set<SimpleGrantedAuthority> authorities;
    private final Boolean enabled;

    public AppUserDetails(User user) {
        id = user.getId();
        username = user.getEmail();
        password = user.getPassword();
        authorities = user.getRole().getAuthorities();
        enabled = user.getEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


}
