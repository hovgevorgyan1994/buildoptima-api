package com.vecondev.buildoptima.service;

import com.amazonaws.services.s3.AmazonS3;
import com.vecondev.buildoptima.config.properties.S3ConfigProperties;
import com.vecondev.buildoptima.exception.ResourceNotFoundException;
import com.vecondev.buildoptima.service.image.impl.ImageServiceImpl;
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
  @InjectMocks private ImageServiceImpl imageService;
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
      imageService.uploadImagesToS3("user", userId, null, userId);
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
      when(s3Client.doesObjectExist(any(), any())).thenReturn(true);
      imageService.uploadImagesToS3("user", userId, null, userId);
    }

    verify(s3Client, times(2)).putObject(any(), any(), any(File.class));
    verify(imageValidator).validateImage(any(), any());
  }

  @Test
  void failedImageUploading() {
    UUID userId = UUID.randomUUID();

    when(s3Client.doesBucketExistV2(any())).thenReturn(false);

    assertThrows(
        ResourceNotFoundException.class,
        () -> imageService.uploadImagesToS3("user", userId, null, userId));
  }

  @Test
  void failedImageDownloadingAsImageDoesntExist() {
    UUID userId = UUID.randomUUID();

    when(s3Client.doesObjectExist(any(), any())).thenReturn(false);

    assertThrows(
        ResourceNotFoundException.class, () -> imageService.downloadImage("user", userId, false));
  }

  @Test
  void successfulImageDeletion() {
    UUID userId = UUID.randomUUID();

    imageService.deleteImagesFromS3("user", userId);

    verify(s3Client, times(2)).deleteObject(any(), any());
  }
}
