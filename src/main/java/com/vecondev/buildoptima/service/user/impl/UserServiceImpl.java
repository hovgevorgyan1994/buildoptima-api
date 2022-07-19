package com.vecondev.buildoptima.service.user.impl;

import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.user.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.user.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.request.user.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.user.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.user.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.user.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.user.UserResponseDto;
import com.vecondev.buildoptima.exception.AuthenticationException;
import com.vecondev.buildoptima.exception.UserNotFoundException;
import com.vecondev.buildoptima.filter.converter.PageableConverter;
import com.vecondev.buildoptima.filter.model.SortDto;
import com.vecondev.buildoptima.filter.specification.GenericSpecification;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.RefreshToken;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.user.UserRepository;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.image.ImageService;
import com.vecondev.buildoptima.service.user.ConfirmationTokenService;
import com.vecondev.buildoptima.service.user.RefreshTokenService;
import com.vecondev.buildoptima.service.user.UserService;
import com.vecondev.buildoptima.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.vecondev.buildoptima.exception.ErrorCode.CONFIRM_TOKEN_NOT_FOUND;
import static com.vecondev.buildoptima.exception.ErrorCode.CREDENTIALS_NOT_FOUND;
import static com.vecondev.buildoptima.exception.ErrorCode.IMAGE_IS_REQUIRED;
import static com.vecondev.buildoptima.exception.ErrorCode.PROVIDED_SAME_PASSWORD;
import static com.vecondev.buildoptima.exception.ErrorCode.PROVIDED_WRONG_PASSWORD;
import static com.vecondev.buildoptima.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;
import static com.vecondev.buildoptima.exception.ErrorCode.SEND_EMAIL_FAILED;
import static com.vecondev.buildoptima.exception.ErrorCode.USER_NOT_FOUND;
import static com.vecondev.buildoptima.filter.model.UserFields.userPageSortingFieldsMap;
import static com.vecondev.buildoptima.util.RestPreconditions.checkNotNull;
import static com.vecondev.buildoptima.validation.validator.FieldNameValidator.validateFieldNames;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final UserValidator userValidator;

  private final PasswordEncoder passwordEncoder;
  private final JwtTokenManager jwtAuthenticationUtil;
  private final ConfirmationTokenService confirmationTokenService;
  private final RefreshTokenService refreshTokenService;
  private final AuthenticationManager authenticationManager;

  private final ImageService imageService;
  private final MailService mailService;
  private final PageableConverter pageableConverter;

  @Override
  public UserResponseDto register(UserRegistrationRequestDto dto, Locale locale) {
    User user = userMapper.mapToEntity(dto);
    userValidator.validateUserRegistration(user);
    user = userRepository.saveAndFlush(user);
    ConfirmationToken confirmationToken = confirmationTokenService.create(user);
    log.info("New user registered.");
    try {
      mailService.sendConfirm(locale, confirmationToken);
    } catch (MessagingException e) {
      throw new AuthenticationException(SEND_EMAIL_FAILED);
    }
    log.info("Verification email was sent to user {}", user.getEmail());
    return userMapper.mapToResponseDto(user);
  }

  @Override
  public UserResponseDto activate(String token) {
    ConfirmationToken confirmationToken = confirmationTokenService.getByToken(token);
    if (confirmationTokenService.isNotValid(confirmationToken)) {
      log.warn("The email confirmation token is not valid");
      throw new AuthenticationException(CONFIRM_TOKEN_NOT_FOUND);
    }
    return activateUserAccount(confirmationToken);
  }

  @Override
  public AuthResponseDto authenticate(final AuthRequestDto authRequestDto) {
    log.info("Request from user {} to get authenticated", authRequestDto.getUsername());
    final Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authRequestDto.getUsername(), authRequestDto.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    AppUserDetails appUser = (AppUserDetails) authentication.getPrincipal();
    return buildAuthDto(appUser);
  }

  @Override
  public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
    log.info("Request to refresh the access token");

    final RefreshToken refreshToken =
        refreshTokenService.findByRefreshToken(request.getRefreshToken());

    if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      log.warn("403 FORBIDDEN response was sent because of an expired refresh token");
      throw new AuthenticationException(REFRESH_TOKEN_EXPIRED);
    }

    User user =
        userRepository
            .findById(refreshToken.getUserId())
            .orElseThrow(() -> new AuthenticationException(CREDENTIALS_NOT_FOUND));
    log.info("Access token is refreshed for user {}", user.getEmail());
    return RefreshTokenResponseDto.builder()
        .accessToken(jwtAuthenticationUtil.generateAccessToken(user))
        .refreshToken(refreshToken.getRefreshToken())
        .build();
  }

  @Override
  public FetchResponseDto fetchUsers(FetchRequestDto fetchRequest) {
    log.info("Request to fetch users from DB");
    validateFieldNames(userPageSortingFieldsMap, fetchRequest.getSort());
    if (fetchRequest.getSort() == null || fetchRequest.getSort().isEmpty()) {
      SortDto sortDto = new SortDto("firstName", SortDto.Direction.ASC);
      fetchRequest.setSort(List.of(sortDto));
    }
    Pageable pageable = pageableConverter.convert(fetchRequest);
    Specification<User> specification =
        new GenericSpecification<>(userPageSortingFieldsMap, fetchRequest.getFilter());

    assert pageable != null;
    Page<User> result = userRepository.findAll(specification, pageable);

    List<UserResponseDto> content = userMapper.mapToResponseList(result);
    log.info("Response was sent. {} results where found", content.size());
    return FetchResponseDto.builder()
        .content(content)
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .last(result.isLast())
        .build();
  }

  @Override
  public void changePassword(ChangePasswordRequestDto request, AppUserDetails userDetails) {
    log.info("Request from user {} to change the password", userDetails.getUsername());
    User user =
        userRepository
            .findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new AuthenticationException(USER_NOT_FOUND));
    if (!isValidPassword(request, user)) {
      log.warn("User {} had provided wrong credentials to change the password", user.getEmail());
      throw new AuthenticationException(PROVIDED_WRONG_PASSWORD);
    }
    if (request.getOldPassword().equals(request.getNewPassword())) {
      log.warn("In change password request user {} provided the same password", user.getEmail());
      throw new AuthenticationException(PROVIDED_SAME_PASSWORD);
    }
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    log.info("User {} password was successfully changed", user.getEmail());
  }

  @Override
  public UserResponseDto getUser(UUID userId) {
    log.info("Request to get user profile by id");
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new AuthenticationException(USER_NOT_FOUND));
    log.info("Fetched user {} profile", user.getEmail());
    return userMapper.mapToResponseDto(user);
  }

  @Override
  public void verifyUserAndSendEmail(ConfirmEmailRequestDto requestDto, Locale locale) {
    log.info(
        "Request from optional user {} to get a password restoring email", requestDto.getEmail());
    User user =
        userRepository
            .findByEmail(requestDto.getEmail())
            .orElseThrow(() -> new AuthenticationException(USER_NOT_FOUND));

    ConfirmationToken token = confirmationTokenService.create(user);
    try {
      log.info("Sending a password restoring email to user {}", user.getEmail());
      mailService.sendVerify(locale, token);
      log.info("Password restoring email was sent to user {}", user.getEmail());
    } catch (MessagingException e) {
      log.error("Something went wrong while sending email to user {}", user.getEmail());
      throw new AuthenticationException(SEND_EMAIL_FAILED);
    }
  }

  @Override
  public void restorePassword(RestorePasswordRequestDto restorePasswordRequestDto) {
    log.info("Request to restore a forgotten password");
    ConfirmationToken confirmationToken =
        confirmationTokenService.getByToken(restorePasswordRequestDto.getConfirmationToken());
    if (confirmationTokenService.isNotValid(confirmationToken)) {
      log.warn("The confirmation token was not found");
      throw new AuthenticationException(CONFIRM_TOKEN_NOT_FOUND);
    }
    User user = confirmationToken.getUser();
    user.setPassword(passwordEncoder.encode(restorePasswordRequestDto.getNewPassword()));
    confirmationTokenService.deleteByUserId(confirmationToken.getUser().getId());
    log.info("User {} has successfully changed the password", user.getEmail());
  }


  /**
   * uploads new image or updates existing one, saves the original one 'and' it's thumbnail version
   * as well
   *
   * @param multipartFile file representing the image
   */
  @Override
  public void uploadImage(UUID userId, MultipartFile multipartFile, AppUserDetails userDetails) {
    checkNotNull(multipartFile, IMAGE_IS_REQUIRED);

    imageService.uploadImagesToS3("user", userId, multipartFile, userDetails.getId());
  }

  /**
   * downloads image
   *
   * @param ownerId the image owner
   * @param isOriginal flag that shows if image is original or not (thumbnail)
   */
  public ResponseEntity<byte[]> downloadImage(UUID ownerId, boolean isOriginal) {
    if (!userRepository.existsById(ownerId)) {
      throw new UserNotFoundException(USER_NOT_FOUND);
    }
    String className = User.class.getSimpleName().toLowerCase();
    byte[] imageAsByteArray = imageService.downloadImage(className, ownerId, isOriginal);
    String contentType = imageService.getContentTypeOfObject("user", ownerId, isOriginal);

    return ResponseEntity.ok()
        .contentLength(imageAsByteArray.length)
        .header("Content-type", contentType)
        .header(
            "Content-disposition",
            String.format(
                "attachment; filename=image%s.%s",
                isOriginal ? "" : "-100X100", contentType.substring(contentType.indexOf("/") + 1)))
        .body(imageAsByteArray);
  }

  @Override
  public void deleteImage(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(USER_NOT_FOUND);
    }

    imageService.deleteImagesFromS3("user", userId);
  }

  public User getUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
  }

  private boolean isValidPassword(ChangePasswordRequestDto request, User user) {
    return passwordEncoder.matches(request.getOldPassword(), user.getPassword());
  }

  private UserResponseDto activateUserAccount(ConfirmationToken confirmationToken) {
    User user = userRepository.getReferenceById(confirmationToken.getUser().getId());
    user.setEnabled(true);
    confirmationTokenService.remove(user.getId());
    log.info("User {} account was verified by email", user.getEmail());
    return userMapper.mapToResponseDto(user);
  }

  private AuthResponseDto buildAuthDto(final AppUserDetails appUser) {
    log.info("User {} provided credentials to receive an access token", appUser.getUsername());
    User user = userRepository.getReferenceById(appUser.getId());
    final String accessToken = jwtAuthenticationUtil.generateAccessToken(user);
    RefreshToken refreshToken = refreshTokenService.findByUserId(user.getId());
    if (refreshToken == null) {
      refreshToken = refreshTokenService.create(user.getId());
    }
    log.info("Access token was created for user {}", user.getEmail());
    return AuthResponseDto.builder()
        .accessToken(accessToken)
        .refreshTokenId(refreshToken.getId().toString())
        .build();
  }
}
