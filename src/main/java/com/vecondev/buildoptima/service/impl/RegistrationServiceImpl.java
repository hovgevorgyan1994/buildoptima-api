package com.vecondev.buildoptima.service.impl;

import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.UserRegistrationResponseDto;
import com.vecondev.buildoptima.error.AuthErrorCode;
import com.vecondev.buildoptima.exception.AuthException;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.UserRepository;
import com.vecondev.buildoptima.service.ConfirmationTokenService;
import com.vecondev.buildoptima.service.MailService;
import com.vecondev.buildoptima.service.RegistrationService;
import com.vecondev.buildoptima.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final UserValidator userValidator;
  private final ConfirmationTokenService confirmationTokenService;
  private final MailService mailService;

  @Override
  @SneakyThrows
  public UserRegistrationResponseDto register(UserRegistrationRequestDto dto, Locale locale) {
    User user = userMapper.mapToEntity(dto);
    userValidator.validateUserRegistration(user);
    user = userRepository.save(user);
    ConfirmationToken confirmationToken = confirmationTokenService.create(user);
    log.info("New user registered.");
    mailService.send(locale, confirmationToken);
    log.info("Verification email was sent to user {}", user.getEmail());
    return userMapper.mapToRegistrationResponseDto(user);
  }

  @Override
  public UserRegistrationResponseDto activate(String token) {
    ConfirmationToken confirmationToken = confirmationTokenService.getByToken(token);
    if (!isValid(confirmationToken)) {
      log.warn("The email confirmation token is not valid");
      throw new AuthException(AuthErrorCode.AUTH_CONFIRM_TOKEN_NOT_FOUND,AuthErrorCode.AUTH_CONFIRM_TOKEN_NOT_FOUND.getMessage());
    }
    return activateUserAccount(confirmationToken);
  }

  private UserRegistrationResponseDto activateUserAccount(ConfirmationToken confirmationToken) {
    User user = userRepository.getReferenceById(confirmationToken.getUser().getId());
    user.setEnabled(true);
    confirmationTokenService.remove(user.getId());
    log.info("User {} account was verified by email", user.getEmail());
    return userMapper.mapToRegistrationResponseDto(user);
  }

  private boolean isValid(ConfirmationToken confirmationToken) {
    Optional<User> user = userRepository.findById(confirmationToken.getUser().getId());
    return user.isPresent() && user.get().getEnabled().equals(false);
  }
}
