package com.vecondev.buildoptima.service;

import com.amazonaws.services.s3.model.S3Object;
import com.vecondev.buildoptima.config.properties.S3ConfigProperties;
import com.vecondev.buildoptima.parameters.property.PropertyServiceTestParams;
import com.vecondev.buildoptima.repository.property.PropertyRepository;
import com.vecondev.buildoptima.service.property.PropertyServiceImpl;
import com.vecondev.buildoptima.service.s3.AmazonS3Service;
import com.vecondev.buildoptima.util.FileUtil;
import com.vecondev.buildoptima.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class PropertyServiceTest {

  private final PropertyServiceTestParams testParams = new PropertyServiceTestParams();
  @InjectMocks private PropertyServiceImpl propertyService;
  @Mock private AmazonS3Service amazonS3Service;

  @Mock private S3ConfigProperties s3ConfigProperties;
  @Mock PropertyRepository propertyRepository;

  @Test
  void migrateFromS3Success() {
    Path path = testParams.convertS3ObjectToPath();

    when(amazonS3Service.getUnprocessedFiles(anyString()))
        .thenReturn(testParams.getObjectsFromBucket());

    try (MockedStatic<JsonUtil> jsonUtil = Mockito.mockStatic(JsonUtil.class);
        MockedStatic<FileUtil> fileUtil = Mockito.mockStatic(FileUtil.class)) {
      jsonUtil
          .when(() -> JsonUtil.readFromJson(any(File.class)))
          .thenReturn(testParams.readFromJson());
      fileUtil.when(() -> FileUtil.convertS3ObjectToPath(any(S3Object.class))).thenReturn(path);
      propertyService.migrateFromS3();
      assertThat(propertyRepository.count() == 3);
    }
  }
}
