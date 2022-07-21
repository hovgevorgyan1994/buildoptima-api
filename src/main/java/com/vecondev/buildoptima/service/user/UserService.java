package com.vecondev.buildoptima.service.user;

import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.user.request.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.user.response.UserResponseDto;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {

  FetchResponseDto fetch (FetchRequestDto viewRequest, String username);

  void changePassword(ChangePasswordRequestDto request, AppUserDetails userDetails);

  UserResponseDto getById (UUID userId);

  void uploadImage(UUID userId, MultipartFile multipartFile, AppUserDetails userDetails);

  ResponseEntity<byte[]> downloadImage(UUID ownerId, boolean isOriginal);

  void deleteImage(UUID userId);

  User findUserById (UUID userId);
}
