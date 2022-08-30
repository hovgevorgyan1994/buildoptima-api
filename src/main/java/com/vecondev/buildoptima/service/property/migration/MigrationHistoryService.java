package com.vecondev.buildoptima.service.property.migration;

import com.vecondev.buildoptima.model.property.migration.MigrationHistory;

public interface MigrationHistoryService {

  MigrationHistory save(String filePath);

  MigrationHistory save(String filePath, String failedReason);

  boolean existsByKey(String key);
}
