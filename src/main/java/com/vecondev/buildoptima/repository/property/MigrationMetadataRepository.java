package com.vecondev.buildoptima.repository.property;

import com.vecondev.buildoptima.model.property.migration.MigrationMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MigrationMetadataRepository extends JpaRepository<MigrationMetadata, UUID> {}
