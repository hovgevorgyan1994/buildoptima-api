package com.vecondev.buildoptima.validation;

import com.vecondev.buildoptima.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;

import static com.vecondev.buildoptima.util.FileUtil.convertMultipartFileToFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageValidator {

  private static final String JPEG_CONTENT_TYPE = "image/jpeg";
  private static final String PNG_CONTENT_TYPE = "image/png";

  public void validateImage(MultipartFile multipartFile, UUID userId) {
    File originalFile = convertMultipartFileToFile(multipartFile);
    BufferedImage originalImage = FileUtil.convertFileToImage(originalFile);

    validateFileExtension(multipartFile, userId);
    validateFileSize(multipartFile, userId);
    validateImageFormat(originalImage, userId);
    validateImageWidthAndHeight(originalImage, userId);
  }

  /**
   * validates if the image provided by user has valid image format or not. e.g. actually can't be
   * represented as image despite its valid extension(.jpg, .png)
   *
   * @throws IllegalArgumentException
   */
  private void validateImageFormat(BufferedImage image, UUID userId) {
    if (image == null) {
      log.warn("User with id: {} provided image with wrong format.", userId);
      throw new IllegalArgumentException("Can't upload the image.");
    }
  }

  /**
   * validates if the image provided by user has minimum required width and height or not. the width
   * should be greater than or equal 600px, and height 600px
   *0
   * @throws IllegalArgumentException
   */
  private void validateImageWidthAndHeight(BufferedImage image, UUID userId) {
    if (image.getWidth() < 600 || image.getHeight() < 600) {
      log.warn("User with id: {} provided image with smaller size than it's required.", userId);
      throw new IllegalArgumentException("Image has smaller size than it's required (600x600).");
    }
  }

  /**
   * validates if the image provided by user has required size (70KB-30MB) or not
   *
   * @param file representing the image
   * @throws IllegalArgumentException
   */
  private void validateFileSize(MultipartFile file, UUID userId) {
    long size = file.getSize();
    if (size < 71680 || size > 31457280) {
      log.warn("User with id: {} provided image with invalid data size.", userId);
      throw new IllegalArgumentException("Image's size should be between 3MB and 30MB.");
    }
  }

  /**
   * validates if the file passed by user has valid extension (.jpg, .jpeg or .png) or not
   *
   * @param file representing the image
   * @throws IllegalArgumentException
   */
  private void validateFileExtension(MultipartFile file, UUID userID) {
    String extension = file.getContentType();

    if (!JPEG_CONTENT_TYPE.equals(extension) && !PNG_CONTENT_TYPE.equals(extension)) {
      log.info(extension);
      log.warn("The image uploaded by user with id: {} has wrong extension.", userID);
      throw new IllegalArgumentException(
          "The extension of the image should be either 'jpg/jpeg' or 'png'.");
    }
  }
}
