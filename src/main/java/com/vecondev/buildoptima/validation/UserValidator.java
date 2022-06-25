package com.vecondev.buildoptima.validation;

import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository repository;

    public void validateUserRegistration(User user){
        validateEmail(user.getEmail());
        validatePhone(user.getPhone());
    }

    private void validateEmail(String email){
        if (repository.existsByEmailIgnoreCase(email)){
            log.warn("Invalid email! There is an user in database with such email.");

            throw new AlreadyBuiltException("There is an user registered with such email!");
        }
    }

    private void validatePhone(String phone){
        if(repository.existsByPhone(phone)){
            log.warn("Invalid phone number! There is an user in database with such phone number.");

            throw new AlreadyBuiltException("There is an user registered with such phone number!");
        }
    }
}
