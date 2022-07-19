package com.vecondev.buildoptima.parameters.user;

import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.user.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.user.UserResponseDto;
import com.vecondev.buildoptima.exception.WrongFieldException;
import com.vecondev.buildoptima.filter.model.SortDto;
import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.RefreshToken;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.PageableTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.vecondev.buildoptima.exception.ErrorCode.INVALID_PAGEABLE;
import static com.vecondev.buildoptima.model.user.Role.CLIENT;

public class UserServiceTestParameters extends UserTestParameters implements PageableTest {
  private final PasswordEncoder encoder = new BCryptPasswordEncoder(12);

  public List<User> getFetchResponse() {
    return List.of(
        User.builder().lastName("Anderson").firstName("John").build(),
        User.builder().firstName("John").build());
  }

  public UserRegistrationRequestDto getUserRegistrationRequestDto() {
    return new UserRegistrationRequestDto(
        "Example", "Example", "+1234567890", "example@gmail.com", "Example1234.");
  }

  public User getUserFromRegistrationDto(UserRegistrationRequestDto requestDto) {
    return new User(
        requestDto.getFirstName(),
        requestDto.getLastName(),
        requestDto.getPhone(),
        requestDto.getEmail(),
        requestDto.getPassword(),
        CLIENT,
        false);
  }


  public User getSavedUser(User user) {
    User savedUser =
        user.toBuilder()
            .password(encoder.encode(user.getPassword()))
            .enabled(true)
            .build();
    savedUser.setId(UUID.randomUUID());

    return savedUser;
  }

  public User getSavedUser() {
    User user =
        new User(
            "Example",
            "Example",
            "+1234567890",
            "Example@gmail.com",
            encoder.encode("Example1234."),
            CLIENT,
            true);
    user.setId(UUID.randomUUID());

    return user;
  }

  public UserResponseDto getUserResponseDto(User user) {
    return new UserResponseDto(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getPhone(),
        user.getEmail(),
        user.getRole(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  public List<UserResponseDto> getUserResponseDtoList(List<User> users) {
    return users.stream().map(this::getUserResponseDto).collect(Collectors.toList());
  }

  public ConfirmationToken getConfirmationToken(String token, User user) {
    return new ConfirmationToken(token, LocalDateTime.now().plusDays(1), user);
  }

  public RefreshToken getRefreshTokenWithRefreshTokenId(
      String tokenId, UUID userId, Boolean isDecoded) {
    RefreshToken refreshToken =
        new RefreshToken(UUID.randomUUID().toString(), userId, LocalDateTime.now().plusMonths(6));
    refreshToken.setId(UUID.fromString(tokenId));

    if (isDecoded) {
      refreshToken.setRefreshToken(encoder.encode(refreshToken.getRefreshToken()));
    }

    return refreshToken;
  }

  public Pageable getPageable(FetchRequestDto fetchRequest) {
    Sort sort = Sort.unsorted();
    int skip = fetchRequest.getSkip() != null ? fetchRequest.getSkip() : 0;
    int take = fetchRequest.getTake() != null ? fetchRequest.getTake() : 10;
    int page = 0;

    if (take > 0) {
      if (skip % take != 0) {
        throw new WrongFieldException(INVALID_PAGEABLE.getMessage());
      }

      page = skip / take;
    }
    if (fetchRequest.getSort() == null) {
      fetchRequest.setSort(new ArrayList<>());
    }
    for (SortDto sortDto : fetchRequest.getSort()) {
      sort =
          sort.and(
              Sort.by(Sort.Direction.fromString(sortDto.getOrder().name()), sortDto.getField()));
    }
    return new PageRequest(page, take, sort) {};
  }

  public ChangePasswordRequestDto getChangePasswordRequestDto(
      String oldPassword, String newPassword) {
    return new ChangePasswordRequestDto(oldPassword, newPassword);
  }

  public String getPasswordEncoded(String password) {
    return encoder.encode(password);
  }

  public ConfirmationToken getSavedConfirmationToken(User savedUser, UUID uuid) {
    return new ConfirmationToken(uuid.toString(), LocalDateTime.now().plusDays(1), savedUser);
  }
}
