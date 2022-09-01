package com.vecondev.buildoptima.api.controller;

import com.vecondev.buildoptima.api.PropertyApi;
import com.vecondev.buildoptima.dto.property.response.PropertyMigrationProgressResponseDto;
import com.vecondev.buildoptima.dto.property.response.PropertyMigrationResponseDto;
import com.vecondev.buildoptima.dto.property.response.PropertyReprocessResponseDto;
import com.vecondev.buildoptima.dto.property.response.PropertyResponseDto;
import com.vecondev.buildoptima.model.property.migration.MigrationHistory;
import com.vecondev.buildoptima.service.property.PropertyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
@PreAuthorize("hasAuthority('resource_write')")
public class PropertyController implements PropertyApi {

  private final PropertyService propertyService;

  @Override
  @PostMapping("/migrate")
  public ResponseEntity<PropertyMigrationResponseDto> migrateUnprocessedFiles() {
    List<MigrationHistory> processedFilesBefore = propertyService.migrateFromS3();

    return ResponseEntity.ok(propertyService.getMigrationResults(processedFilesBefore));
  }

  @Override
  @PostMapping("/re-process")
  public ResponseEntity<PropertyReprocessResponseDto> reprocessFailedToProcessFiles() {
    List<MigrationHistory> failedFilesBefore = propertyService.reprocessFailedFiles();

    return ResponseEntity.ok(propertyService.getReprocessResults(failedFilesBefore));
  }

  @Override
  @GetMapping("/progress")
  public ResponseEntity<PropertyMigrationProgressResponseDto> trackMigrationProgress() {
    return ResponseEntity.ok(propertyService.getMigrationProgress());
  }

  @Override
  @GetMapping("/{ain}")
  public ResponseEntity<PropertyResponseDto> getByAin(@PathVariable String ain) {
    return ResponseEntity.ok(propertyService.getByAin(ain));
  }
}
