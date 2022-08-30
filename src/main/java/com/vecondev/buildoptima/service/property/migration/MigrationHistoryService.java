package com.vecondev.buildoptima.service.property.migration;

import com.vecondev.buildoptima.model.property.migration.MigrationHistory;
import java.util.List;

public interface MigrationHistoryService {

  MigrationHistory saveSucceededHistory(String filePath);

  MigrationHistory saveFailedHistory(String filePath, String failedReason);

  boolean existsByKey(String key);

  List<MigrationHistory> findAll();

  List<MigrationHistory> findAllByFailedAtNotNull();

  void deleteByFilePath(String filePath);
}
