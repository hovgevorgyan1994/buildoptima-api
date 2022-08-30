package com.vecondev.buildoptima.service.property.migration.impl;

import com.vecondev.buildoptima.model.property.migration.MigrationHistory;
import com.vecondev.buildoptima.repository.property.MigrationHistoryRepository;
import com.vecondev.buildoptima.service.property.migration.MigrationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = Exception.class)
public class MigrationHistoryServiceImpl implements MigrationHistoryService {

  private final MigrationHistoryRepository historyRepository;

  @Override
  public MigrationHistory save(String filePath) {
    MigrationHistory migrationHistory =
        historyRepository.save(MigrationHistory.builder().filePath(filePath).build());
    log.info("Successfully saved succeeded processing history of file {}", filePath);
    return migrationHistory;
  }

  @Override
  public MigrationHistory save(String filePath, String failedReason) {
    MigrationHistory migrationHistory =
        MigrationHistory.builder()
            .filePath(filePath)
            .failedAt(Instant.now())
            .failedReason(failedReason)
            .build();
    MigrationHistory save = historyRepository.save(migrationHistory);
    log.info("Successfully saved failed processing history of file {}", filePath);
    return save;
  }

  @Override
  public boolean existsByKey(String filePath) {
    return historyRepository.existsByFilePath(filePath);
  }
}
