package com.vecondev.buildoptima.security.user;

import com.vecondev.buildoptima.exception.UserNotFoundException;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.vecondev.buildoptima.exception.ErrorCode.BAD_CREDENTIALS;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

  private final UserRepository repository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        repository
            .findByEmail(username)
            .orElseThrow(() -> new UserNotFoundException(BAD_CREDENTIALS));

    return new AppUserDetails(user);
  }
}
