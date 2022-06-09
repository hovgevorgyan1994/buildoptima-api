package com.vecondev.buildoptima.security.user;

import com.vecondev.buildoptima.exception.UserNotFoundException;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByEmail(username)
                .orElseThrow(UserNotFoundException::new);

        return new AppUserDetails(user);
    }
}
