package com.vecondev.buildoptima.service.property;

import com.amazonaws.services.s3.model.S3Object;
import com.google.common.collect.Lists;
import com.vecondev.buildoptima.config.properties.S3ConfigProperties;
import com.vecondev.buildoptima.dto.property.PropertyListDto;
import com.vecondev.buildoptima.dto.property.PropertyReadDto;
import com.vecondev.buildoptima.mapper.property.PropertyMapper;
import com.vecondev.buildoptima.model.property.Address;
import com.vecondev.buildoptima.model.property.Property;
import com.vecondev.buildoptima.model.property.migration.MigrationHistory;
import com.vecondev.buildoptima.repository.property.PropertyRepository;
import com.vecondev.buildoptima.service.property.migration.MigrationHistoryService;
import com.vecondev.buildoptima.service.property.migration.MigrationMetadataService;
import com.vecondev.buildoptima.service.s3.AmazonS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.vecondev.buildoptima.util.FileUtil.*;
import static com.vecondev.buildoptima.util.JsonUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

  private final PropertyMapper propertyMapper;
  private final PropertyRepository propertyRepository;
  private final MigrationHistoryService migrationHistoryService;
  private final MigrationMetadataService migrationMetadataService;
  private final AmazonS3Service amazonS3Service;
  private final S3ConfigProperties s3ConfigProperties;

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE, noRollbackFor = Exception.class)
  public void migrateFromS3() {
    List<S3Object> unprocessedFiles =
        amazonS3Service.getUnprocessedFiles(s3ConfigProperties.getDataBucketName());
    log.info("Retrieved {} property files from S3 to process", unprocessedFiles.size());
    int size = unprocessedFiles.size();
    if (size != 0) {
      ExecutorService executorService = Executors.newFixedThreadPool(size);
      processFiles(unprocessedFiles, size, executorService);
      log.info("{} property files from S3 were processed", unprocessedFiles.size());
    } else {
      log.info("No new property files were found in S3 bucket");
    }
  }

  private void processFiles(
      List<S3Object> unprocessedFiles, int size, ExecutorService executorService) {
    Lists.partition(unprocessedFiles, size)
        .forEach(
            s3Objects -> {
              Runnable runnable =
                  () ->
                      s3Objects.forEach(
                          s3Object -> {
                            try {
                              PropertyListDto propertyListDto =
                                  readFromJson(convertS3ObjectToPath(s3Object).toFile());
                              MigrationHistory migrationHistory =
                                  migrationHistoryService.save(s3Object.getKey());
                              syncProperties(propertyListDto, migrationHistory);
                              log.info(
                                  "{} file from S3 was successfully processed and removed from local storage",
                                  s3Object.getKey());
                            } catch (Exception e) {
                              migrationHistoryService.save(s3Object.getKey(), e.getMessage());
                              log.info(
                                  "Failed processing {} file from S3. See failed reason in migration metadata",
                                  s3Object.getKey());
                            }
                          });
              executorService.submit(runnable);
            });
  }

  private void syncProperties(PropertyListDto propertyListDto, MigrationHistory migrationHistory) {
    List<PropertyReadDto> properties = propertyListDto.getProperties();
    properties.forEach(
        propertyDto -> {
          try {
            Property property = saveOrUpdate(propertyDto);
            migrationMetadataService.save(migrationHistory, property);
          } catch (Exception e) {
            migrationMetadataService.save(migrationHistory, propertyDto, e.getMessage());
          }
        });
  }

  private Property saveOrUpdate(PropertyReadDto propertyDto) {
    Optional<Property> fromDb = propertyRepository.findById(propertyDto.getAin());
    if (fromDb.isEmpty()) {
      return propertyRepository.save(propertyMapper.mapToEntity(propertyDto));
    } else {
      return update(propertyDto, fromDb.get());
    }
  }

  private Property update(PropertyReadDto propertyDto, Property toUpdate) {
    Property property = propertyMapper.mapToEntity(propertyDto);
    List<Address> addresses = toUpdate.getAddresses();
    toUpdate.removeAddresses(addresses);
    toUpdate.setMunicipality(property.getMunicipality());
    toUpdate.addAddresses(property.getAddresses());
    toUpdate.setLocations(property.getLocations());
    toUpdate.setDetails(property.getDetails());
    toUpdate.setHazards(property.getHazards());
    toUpdate.setZoningDetails(property.getZoningDetails());
    return propertyRepository.saveAndFlush(toUpdate);
  }
}
