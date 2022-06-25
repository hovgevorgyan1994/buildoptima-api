package com.vecondev.buildoptima.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ImageService {

  void uploadUserImagesToS3(UUID userID, MultipartFile multipartFile);

  byte[] downloadUserImage(UUID userId, Boolean isOriginal);

  String getContentTypeOfObject(UUID userId, boolean isOriginal);

  void deleteUserImagesFromS3(UUID userId);
}
