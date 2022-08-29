package com.vecondev.buildoptima.service.image;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ImageService {

  void uploadImagesToS3(
      String className,
      UUID objectId,
      Integer imageVersion,
      MultipartFile multipartFile,
      UUID userId);

  byte[] downloadImage(String className, UUID objectId, Integer imageVersion, Boolean isOriginal);

  String getContentTypeOfObject(
      String className, UUID objectId, Integer imageVersion, boolean isOriginal);

  void deleteImagesFromS3(String className, UUID objectId, Integer imageVersion);

  void checkExistenceOfObject(String imageName, UUID userId);

  String getImagePath(String className, UUID objectId, Integer imageVersion, boolean isOriginal);
}
