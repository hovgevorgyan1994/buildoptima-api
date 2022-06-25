package com.vecondev.buildoptima.service.impl;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.vecondev.buildoptima.config.properties.S3ConfigProperties;
import com.vecondev.buildoptima.exception.ResourceNotFoundException;
import com.vecondev.buildoptima.service.ImageService;
import com.vecondev.buildoptima.validation.ImageValidator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.UnexpectedTypeException;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.vecondev.buildoptima.util.FileUtil.convertMultipartFileToFile;
import static com.vecondev.buildoptima.util.FileUtil.deleteFile;
import static com.vecondev.buildoptima.util.FileUtil.resizePhoto;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Data
@RequiredArgsConstructor
@Service
public class ImageServiceImpl implements ImageService {

  private final S3ConfigProperties s3ConfigProperties;
  private final AmazonS3Client s3Client;
  private final ImageValidator imageValidator;

  /**
   * checks the existence of the bucket, uploads original image 'and' it's thumbnail version by
   * resizing with size of 100x100, after all these actions deletes created images from classpath
   *
   * @param userId the user want to upload his/her images
   * @param multipartFile representing images
   */
  @Override
  public void uploadUserImagesToS3(UUID userId, MultipartFile multipartFile) {
    checkExistenceOfBucket();
    imageValidator.validateImage(multipartFile, userId);

    File originalFile = convertMultipartFileToFile(multipartFile);
    uploadUserImage(originalFile, userId, true);

    File thumbnailFile = resizePhoto(originalFile);
    uploadUserImage(thumbnailFile, userId, false);

    deleteFile(originalFile);
    deleteFile(thumbnailFile);
  }

  /**
   * downloads image from s3 by given user
   *
   * @param userId the resource owner
   * @param isOriginal shows if the image is original or not (thumbnail)
   * @return byte[] image as byte array
   */
  @Override
  public byte[] downloadUserImage(UUID userId, Boolean isOriginal) {
    String imageName = getImagePath(userId, isOriginal);
    chekExistenceOfObject(imageName, userId);

    S3Object object = s3Client.getObject(s3ConfigProperties.getBucketName(), imageName);
    S3ObjectInputStream inputStream = object.getObjectContent();
    byte[] objectAsByteArray = null;

    try {
      objectAsByteArray = IOUtils.toByteArray(inputStream);
    } catch (IOException ex) {
      log.error("Error while downloading photo of user with id: {}.", userId);
      throw new UnexpectedTypeException();
    }

    return objectAsByteArray;
  }

  @Override
  public String getContentTypeOfObject(UUID userId, boolean isOriginal) {
    String imageName = getImagePath(userId, isOriginal);

    return s3Client
        .getObject(s3ConfigProperties.getBucketName(), imageName)
        .getObjectMetadata()
        .getContentType();
  }

  /**
   * deletes both images the original and thumbnail
   *
   * @param userId the user whose images should be deleted
   */
  @Override
  public void deleteUserImagesFromS3(UUID userId) {
    deleteUserPhoto(userId, true);
    deleteUserPhoto(userId, false);
  }

  private void checkExistenceOfBucket() {
    if (!s3Client.doesBucketExist(s3ConfigProperties.getBucketName())) {
      log.error("The 'buildoptima' bucket doesn't exist!");
      throw new UnexpectedTypeException();
    }
  }

  /**
   * checks if there is an object with given image name in s3 bucket or not
   *
   * @throws ResourceNotFoundException when no image found by given image name
   */
  private void chekExistenceOfObject(String imageName, UUID userId) {
    if (!s3Client.doesObjectExist(s3ConfigProperties.getBucketName(), imageName)) {
      log.warn("There is no image of user with id: {} to remove.", userId);
      throw new ResourceNotFoundException("There isn't image for the given user.", NOT_FOUND);
    }
  }

  private void uploadUserImage(File file, UUID userId, boolean isOriginal) {
    String imageVersion =
        isOriginal
            ? s3ConfigProperties.getOriginalImagePath()
            : s3ConfigProperties.getThumbnailImagePath();
    String imagePath = getImagePath(userId, isOriginal);
    String imageType =
        imageVersion.substring(imageVersion.indexOf("/") + 1, imageVersion.length() - 1);

    if (s3Client.doesObjectExist(s3ConfigProperties.getBucketName(), imagePath)) {
      log.info("The old {} image of user: {} is deleted.", imageType, userId);
      s3Client.deleteObject(s3ConfigProperties.getBucketName(), imagePath);
    }

    log.info("The user with id: {} uploaded new {} photo.", userId, imageType);
    s3Client.putObject(s3ConfigProperties.getBucketName(), imagePath, file);
  }

  private void deleteUserPhoto(UUID userId, boolean isOriginal) {
    String imageName = getImagePath(userId, isOriginal);
    chekExistenceOfObject(imageName, userId);

    s3Client.deleteObject(s3ConfigProperties.getBucketName(), imageName);
    log.info(
        "The user's(id: {}) {} image is successfully deleted.",
        userId, isOriginal ? "original" : "thumbnail");
  }

  /**
   * forms the path image should be saved in S3 bucket
   *
   * @param isOriginal whether it's original (true) or thumbnail version
   * @return the image path in S3
   */
  private String getImagePath(UUID userID, boolean isOriginal) {
    String imageVersion =
        isOriginal
            ? s3ConfigProperties.getOriginalImagePath()
            : s3ConfigProperties.getThumbnailImagePath();

    return String.format("%s%s", imageVersion, userID);
  }

}
