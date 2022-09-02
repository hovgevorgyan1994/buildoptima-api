package com.vecondev.buildoptima.service.property;

import com.vecondev.buildoptima.dto.property.response.PropertyMigrationProgressResponseDto;
import com.vecondev.buildoptima.dto.property.response.PropertyMigrationResponseDto;
import com.vecondev.buildoptima.dto.property.response.PropertyReprocessResponseDto;
import com.vecondev.buildoptima.model.property.migration.MigrationHistory;
import java.util.List;

public interface PropertyMigrationService {

  List<MigrationHistory> migrateFromS3();

  List<MigrationHistory> reprocessFailedFiles();

  PropertyMigrationResponseDto getMigrationResults(List<MigrationHistory> processedFilesBefore);

  PropertyReprocessResponseDto getReprocessResults(List<MigrationHistory> failedFilesBefore);

  PropertyMigrationProgressResponseDto getMigrationProgress();
}
