package com.vecondev.buildoptima.service.user;

import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.user.request.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.user.response.UserResponseDto;
import com.vecondev.buildoptima.exception.AuthenticationException;
import com.vecondev.buildoptima.exception.UserNotFoundException;
import com.vecondev.buildoptima.filter.converter.PageableConverter;
import com.vecondev.buildoptima.filter.model.SortDto;
import com.vecondev.buildoptima.filter.specification.GenericSpecification;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.user.UserRepository;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.vecondev.buildoptima.exception.Error.IMAGE_IS_REQUIRED;
import static com.vecondev.buildoptima.exception.Error.PROVIDED_SAME_PASSWORD;
import static com.vecondev.buildoptima.exception.Error.PROVIDED_WRONG_PASSWORD;
import static com.vecondev.buildoptima.exception.Error.USER_NOT_FOUND;
import static com.vecondev.buildoptima.filter.model.UserFields.userPageSortingFieldsMap;
import static com.vecondev.buildoptima.util.RestPreconditions.*;
import static com.vecondev.buildoptima.validation.validator.FieldNameValidator.validateFieldNames;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final ImageService imageService;
  private final PageableConverter pageableConverter;

  @Override
  public FetchResponseDto fetch(FetchRequestDto fetchRequest, String username) {
    log.info("User {} is trying to fetch users", username);
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
  public UserResponseDto getById(UUID userId) {
    log.info("Request to get user profile by id");
    User user = findUserById(userId);
    log.info("Fetched user {} profile", user.getEmail());

    return userMapper.mapToResponseDto(user);
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
  @Override
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
    String className = User.class.getSimpleName().toLowerCase();

    imageService.checkExistenceOfObject(imageService.getImagePath(className, userId, true), userId);
    imageService.checkExistenceOfObject(
        imageService.getImagePath(className, userId, false), userId);
    imageService.deleteImagesFromS3("user", userId);
  }

  @Override
  public User findUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
  }

  private boolean isValidPassword(ChangePasswordRequestDto request, User user) {
    return passwordEncoder.matches(request.getOldPassword(), user.getPassword());
  }
}
