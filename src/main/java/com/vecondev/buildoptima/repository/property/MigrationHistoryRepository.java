package com.vecondev.buildoptima.repository.property;

import com.vecondev.buildoptima.model.property.migration.MigrationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface MigrationHistoryRepository extends JpaRepository<MigrationHistory, UUID> {
    boolean existsByFilePath(String key);
}
