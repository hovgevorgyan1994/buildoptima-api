package com.vecondev.buildoptima.service.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.vecondev.buildoptima.config.properties.S3ConfigProperties;
import com.vecondev.buildoptima.exception.ConvertingFailedException;
import com.vecondev.buildoptima.exception.ResourceNotFoundException;
import com.vecondev.buildoptima.validation.ImageValidator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.vecondev.buildoptima.exception.Error.BUCKET_NOT_FOUND;
import static com.vecondev.buildoptima.exception.Error.FAILED_IMAGE_CONVERTING;
import static com.vecondev.buildoptima.exception.Error.IMAGE_NOT_FOUND;
import static com.vecondev.buildoptima.util.FileUtil.*;

@Slf4j
@Data
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

  private final S3ConfigProperties s3ConfigProperties;
  private final AmazonS3 amazonS3;
  private final ImageValidator imageValidator;

  private static final String ORIGINAL = "original";
  private static final String THUMBNAIL = "thumbnail";

  /**
   * checks the existence of the bucket, uploads original image 'and' it's thumbnail version by
   * resizing with size of 100x100, after all these actions deletes created images from classpath
   *
   * @param objectId the id of the image owner entity
   * @param multipartFile representing images
   */
  @Override
  public void uploadImagesToS3(
      String className, UUID objectId, MultipartFile multipartFile, UUID userId) {
    checkExistenceOfBucket();
    imageValidator.validateImage(multipartFile, userId);
    File originalFile = convertMultipartFileToFile(multipartFile);
    uploadImage(className, originalFile, objectId, true);
    File thumbnailFile = resizePhoto(originalFile);
    uploadImage(className, thumbnailFile, objectId, false);

    deleteFile(originalFile);
    deleteFile(thumbnailFile);
  }

  /**
   * downloads image from s3 by given user
   *
   * @param objectId the resource owner
   * @param isOriginal shows if the image is original or not (thumbnail)
   * @return byte[] image as byte array
   */
  @Override
  public byte[] downloadImage(String className, UUID objectId, Boolean isOriginal) {
    String imageName = getImagePath(className, objectId, isOriginal);
    checkExistenceOfObject(imageName, objectId);
    S3Object object = amazonS3.getObject(s3ConfigProperties.getBucketName(), imageName);
    S3ObjectInputStream inputStream = object.getObjectContent();
    byte[] objectAsByteArray;

    try {
      objectAsByteArray = IOUtils.toByteArray(inputStream);
    } catch (IOException ex) {
      log.error("Error while downloading photo of {} with id: {}.", className, objectId);
      throw new ConvertingFailedException(FAILED_IMAGE_CONVERTING);
    }

    return objectAsByteArray;
  }

  @Override
  public String getContentTypeOfObject(String className, UUID objectId, boolean isOriginal) {
    String imageName = getImagePath(className, objectId, isOriginal);

    return amazonS3
        .getObject(s3ConfigProperties.getBucketName(), imageName)
        .getObjectMetadata()
        .getContentType();
  }

  /**
   * deletes both images the original and thumbnail
   *
   * @param objectId the image owner id which should be deleted
   */
  @Override
  public void deleteImagesFromS3(String className, UUID objectId) {
    deleteImage(className, objectId, true);
    deleteImage(className, objectId, false);
  }

  /**
   * checks if there is an object with given image name in s3 bucket or not
   *
   * @throws ResourceNotFoundException when no image found by given image name
   */
  @Override
  public void checkExistenceOfObject(String imagePath, UUID userId) {
    if (!amazonS3.doesObjectExist(s3ConfigProperties.getBucketName(), imagePath)) {
      log.warn("There is no image of user with id: {} to remove.", userId);
      throw new ResourceNotFoundException(IMAGE_NOT_FOUND);
    }
  }

  /**
   * forms the path image should be saved in S3 bucket
   *
   * @param isOriginal whether it's original (true) or thumbnail version
   * @return the image path in S3
   */
  @Override
  public String getImagePath(String className, UUID objectId, boolean isOriginal) {
    return String.format("%s/%s/%s", className, objectId, isOriginal ? ORIGINAL : THUMBNAIL);
  }

  private void checkExistenceOfBucket() {
    if (!amazonS3.doesBucketExistV2(s3ConfigProperties.getBucketName())) {
      log.error("The 'buildoptima' bucket doesn't exist!");
      throw new ResourceNotFoundException(BUCKET_NOT_FOUND);
    }
  }

  private void uploadImage(String className, File file, UUID objectId, boolean isOriginal) {
    String imagePath = getImagePath(className, objectId, isOriginal);

    if (amazonS3.doesObjectExist(s3ConfigProperties.getBucketName(), imagePath)) {
      log.info("The old {} image of item: {} is deleted.", imagePath, objectId);
    }

    amazonS3.putObject(s3ConfigProperties.getBucketName(), imagePath, file);
    log.info("New picture has been uploaded for news item {}", imagePath);
  }

  private void deleteImage(String className, UUID objectId, boolean isOriginal) {
    String imageName = getImagePath(className, objectId, isOriginal);

    amazonS3.deleteObject(s3ConfigProperties.getBucketName(), imageName);
    log.info(
        "The (id: {}) {} image is successfully deleted.",
        objectId,
        isOriginal ? ORIGINAL : THUMBNAIL);
  }
}
