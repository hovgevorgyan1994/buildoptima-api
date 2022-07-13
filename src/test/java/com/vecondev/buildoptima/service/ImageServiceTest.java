package com.vecondev.buildoptima.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.vecondev.buildoptima.config.properties.S3ConfigProperties;
import com.vecondev.buildoptima.exception.ResourceNotFoundException;
import com.vecondev.buildoptima.service.image.ImageService;
import com.vecondev.buildoptima.util.FileUtil;
import com.vecondev.buildoptima.validation.ImageValidator;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

  private static final String ORIGINAL_IMAGES_PATH = "user/original/";
  private static final String THUMBNAIL_IMAGES_PATH = "user/thumbnail/";
  @InjectMocks private ImageService.ImageServiceImpl imageService;
  @Mock private ImageValidator imageValidator;
  @Mock private AmazonS3 s3Client;
  @Mock private S3ConfigProperties s3ConfigProperties;


  @Test
  void successfulImageUploading() {
    UUID userId = UUID.randomUUID();

    try (MockedStatic<FileUtil> fileUtil = Mockito.mockStatic(FileUtil.class)) {
      fileUtil
          .when(() -> FileUtil.convertMultipartFileToFile(any()))
          .thenReturn(Files.newTemporaryFile());
      fileUtil.when(() -> FileUtil.resizePhoto(any())).thenReturn(Files.newTemporaryFile());
      when(s3Client.doesBucketExistV2(any())).thenReturn(true);
      when(s3Client.doesObjectExist(any(), any())).thenReturn(false);
      when(s3ConfigProperties.getOriginalImagePath()).thenReturn(ORIGINAL_IMAGES_PATH);
      when(s3ConfigProperties.getThumbnailImagePath()).thenReturn(THUMBNAIL_IMAGES_PATH);
      imageService.uploadUserImagesToS3(userId, null);
    }

    verify(s3Client, times(2)).putObject(any(), any(), any(File.class));
    verify(imageValidator).validateImage(any(), any());
  }

  @Test
  void successfulImageUpdating() {
    UUID userId = UUID.randomUUID();

    try (MockedStatic<FileUtil> fileUtil = Mockito.mockStatic(FileUtil.class)) {
      fileUtil
          .when(() -> FileUtil.convertMultipartFileToFile(any()))
          .thenReturn(Files.newTemporaryFile());
      fileUtil.when(() -> FileUtil.resizePhoto(any())).thenReturn(Files.newTemporaryFile());
      when(s3Client.doesBucketExistV2(any())).thenReturn(true);
      when(s3Client.doesObjectExist(any(), any())).thenReturn(false);
      when(s3ConfigProperties.getOriginalImagePath()).thenReturn(ORIGINAL_IMAGES_PATH);
      when(s3ConfigProperties.getThumbnailImagePath()).thenReturn(THUMBNAIL_IMAGES_PATH);
      imageService.uploadUserImagesToS3(userId, null);
    }

    verify(s3Client, times(2)).putObject(any(), any(), any(File.class));
    verify(imageValidator).validateImage(any(), any());
  }

  @Test
  void failedImageUploading() {
    UUID userId = UUID.randomUUID();

    when(s3Client.doesBucketExistV2(any())).thenReturn(false);

    assertThrows(
        ResourceNotFoundException.class, () -> imageService.uploadUserImagesToS3(userId, null));
  }

  @Test
  void failedImageDownloading() {
    UUID userId = UUID.randomUUID();
    boolean isOriginal = false;

    when(s3ConfigProperties.getThumbnailImagePath()).thenReturn(THUMBNAIL_IMAGES_PATH);
    when(s3Client.doesObjectExist(any(), any())).thenReturn(true);
    when(s3Client.getObject(any(), any(String.class))).thenReturn(new S3Object());

    assertThrows(
        NullPointerException.class, () -> imageService.downloadUserImage(userId, isOriginal));
  }

  @Test
  void failedImageDownloadingAsImageDoesntExist() {
    UUID userId = UUID.randomUUID();
    boolean isOriginal = false;

    when(s3ConfigProperties.getThumbnailImagePath()).thenReturn(THUMBNAIL_IMAGES_PATH);
    when(s3Client.doesObjectExist(any(), any())).thenReturn(isOriginal);

    assertThrows(
        ResourceNotFoundException.class, () -> imageService.downloadUserImage(userId, isOriginal));
  }

  @Test
  void succesfulImageDeletion() {
    UUID userId = UUID.randomUUID();

    when(s3ConfigProperties.getOriginalImagePath()).thenReturn(ORIGINAL_IMAGES_PATH);
    when(s3ConfigProperties.getThumbnailImagePath()).thenReturn(THUMBNAIL_IMAGES_PATH);
    when(s3Client.doesObjectExist(any(), any())).thenReturn(true);
    imageService.deleteUserImagesFromS3(userId);

    verify(s3Client, times(2)).deleteObject(any(), any());
  }
}
