package com.vecondev.buildoptima.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.vecondev.buildoptima.config.properties.S3ConfigProperties;
import com.vecondev.buildoptima.exception.ConvertingFailedException;
import com.vecondev.buildoptima.exception.ResourceNotFoundException;
import com.vecondev.buildoptima.service.property.migration.MigrationHistoryService;
import com.vecondev.buildoptima.validation.ImageValidator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.vecondev.buildoptima.exception.Error.*;
import static com.vecondev.buildoptima.util.FileUtil.*;

@Slf4j
@Data
@Service
@RequiredArgsConstructor
public class AmazonS3ServiceImpl implements AmazonS3Service {

  private final S3ConfigProperties s3ConfigProperties;
  private final AmazonS3 amazonS3;
  private final ImageValidator imageValidator;
  private final MigrationHistoryService migrationHistoryService;
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
      String className,
      UUID objectId,
      Integer imageVersion,
      MultipartFile multipartFile,
      UUID userId) {
    checkExistenceOfBucket(s3ConfigProperties.getImageBucketName());
    imageValidator.validateImage(multipartFile, userId);
    File originalFile = convertMultipartFileToFile(multipartFile);
    uploadImage(className, originalFile, objectId, imageVersion + 1, true);
    File thumbnailFile = resizePhoto(originalFile);
    uploadImage(className, thumbnailFile, objectId, imageVersion + 1, false);

    if (amazonS3.doesObjectExist(
        s3ConfigProperties.getImageBucketName(),
        getImagePath(className, objectId, imageVersion, true))) {
      deleteImage(className, objectId, imageVersion, true);
    }
    if (amazonS3.doesObjectExist(
        s3ConfigProperties.getImageBucketName(),
        getImagePath(className, objectId, imageVersion, false))) {
      deleteImage(className, objectId, imageVersion, false);
    }
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
  public byte[] downloadImage(
      String className, UUID objectId, Integer imageVersion, Boolean isOriginal) {
    String imageName = getImagePath(className, objectId, imageVersion, isOriginal);
    checkExistenceOfObject(imageName, objectId);
    S3Object object = amazonS3.getObject(s3ConfigProperties.getImageBucketName(), imageName);
    S3ObjectInputStream inputStream = object.getObjectContent();
    byte[] objectAsByteArray;

    try (inputStream) {
      objectAsByteArray = IOUtils.toByteArray(inputStream);
    } catch (IOException ex) {
      log.error("Error while downloading photo of {} with id: {}.", className, objectId);
      throw new ConvertingFailedException(FAILED_IMAGE_CONVERTING);
    }

    return objectAsByteArray;
  }

  @Override
  public String getContentTypeOfObject(
      String className, UUID objectId, Integer imageVersion, boolean isOriginal) {
    String imageName = getImagePath(className, objectId, imageVersion, isOriginal);

    return amazonS3
        .getObject(s3ConfigProperties.getImageBucketName(), imageName)
        .getObjectMetadata()
        .getContentType();
  }

  /**
   * deletes both images the original and thumbnail
   *
   * @param objectId the image owner id which should be deleted
   */
  @Override
  public void deleteImagesFromS3(String className, UUID objectId, Integer imageVersion) {
    deleteImage(className, objectId, imageVersion, true);
    deleteImage(className, objectId, imageVersion, false);
  }

  /**
   * checks if there is an object with given image name in s3 bucket or not
   *
   * @throws ResourceNotFoundException when no image found by given image name
   */
  @Override
  public void checkExistenceOfObject(String imagePath, UUID userId) {
    if (!amazonS3.doesObjectExist(s3ConfigProperties.getImageBucketName(), imagePath)) {
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
  public String getImagePath(
      String className, UUID objectId, Integer imageVersion, boolean isOriginal) {
    return String.format(
        "%s/%s/%s/%s", className, objectId, isOriginal ? ORIGINAL : THUMBNAIL, imageVersion);
  }

  @Override
  public List<S3Object> getUnprocessedFiles(String bucketName) {
    checkExistenceOfBucket(bucketName);
    return amazonS3.listObjectsV2(bucketName).getObjectSummaries().stream()
        .map(S3ObjectSummary::getKey)
        .filter(key -> !migrationHistoryService.existsByKey(key))
        .map(key -> amazonS3.getObject(bucketName, key))
        .toList();
  }

  private void checkExistenceOfBucket(String bucketName) {
    if (!amazonS3.doesBucketExistV2(bucketName)) {
      log.error("The '{}' bucket doesn't exist!", bucketName);
      throw new ResourceNotFoundException(BUCKET_NOT_FOUND);
    }
  }

  private void uploadImage(
      String className, File file, UUID objectId, Integer imageVersion, boolean isOriginal) {
    String imagePath = getImagePath(className, objectId, imageVersion, isOriginal);

    if (amazonS3.doesObjectExist(s3ConfigProperties.getImageBucketName(), imagePath)) {
      log.info("The old {} image of item: {} is deleted.", imagePath, objectId);
    }

    amazonS3.putObject(s3ConfigProperties.getImageBucketName(), imagePath, file);
    log.info("New picture has been uploaded for news item {}", imagePath);
  }

  private void deleteImage(
      String className, UUID objectId, Integer imageVersion, boolean isOriginal) {
    String imageName = getImagePath(className, objectId, imageVersion, isOriginal);

    amazonS3.deleteObject(s3ConfigProperties.getImageBucketName(), imageName);
    log.info(
        "The (id: {}) {} image is successfully deleted.",
        objectId,
        isOriginal ? ORIGINAL : THUMBNAIL);
  }
}
