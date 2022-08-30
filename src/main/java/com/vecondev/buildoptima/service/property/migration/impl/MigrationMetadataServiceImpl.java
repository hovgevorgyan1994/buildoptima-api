package com.vecondev.buildoptima.service.property.migration.impl;

import com.vecondev.buildoptima.dto.property.PropertyReadDto;
import com.vecondev.buildoptima.mapper.property.AddressMapper;
import com.vecondev.buildoptima.model.property.Address;
import com.vecondev.buildoptima.model.property.Property;
import com.vecondev.buildoptima.model.property.migration.MigrationHistory;
import com.vecondev.buildoptima.model.property.migration.MigrationMetadata;
import com.vecondev.buildoptima.repository.property.MigrationMetadataRepository;
import com.vecondev.buildoptima.service.property.migration.MigrationMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = Exception.class)
public class MigrationMetadataServiceImpl implements MigrationMetadataService {

  private final MigrationMetadataRepository repository;
  private final AddressMapper addressMapper;

  @Override
  public MigrationMetadata save(MigrationHistory migrationHistory, Property property) {
    MigrationMetadata migrationMetadata =
        MigrationMetadata.builder()
            .ain(property.getAin())
            .syncedAt(Instant.now())
            .migrationHistory(migrationHistory)
            .build();
    return saveSucceeded(property, migrationMetadata);
  }

  @Override
  public MigrationMetadata save(
      MigrationHistory migrationHistory, PropertyReadDto property, String failedReason) {
    MigrationMetadata migrationMetadata =
        MigrationMetadata.builder()
            .ain(property.getAin())
            .syncedAt(Instant.now())
            .migrationHistory(migrationHistory)
            .failedAt(Instant.now())
            .failedReason(failedReason)
            .build();
    return saveFailed(property, migrationMetadata);
  }

  private MigrationMetadata saveSucceeded(Property property, MigrationMetadata migrationMetadata) {
    if (!property.getAddresses().isEmpty() && property.getAddresses() != null) {
      migrationMetadata.setAddresses(addressMapper.mapToDtoList(property.getAddresses()));
    }
    return repository.save(migrationMetadata);
  }

  private MigrationMetadata saveFailed(
      PropertyReadDto property, MigrationMetadata migrationMetadata) {
    if (property.getAssociatedAddresses() != null && !property.getAssociatedAddresses().isEmpty()) {
      List<Address> addresses = property.getAssociatedAddresses();
      if (addresses == null || addresses.isEmpty()) {
        migrationMetadata.setAddresses(Collections.emptyList());
      } else {
        addresses.forEach(
            address -> {
              if (address != null) address.setPrimary(false);
            });
        Address primaryAddress = property.getPropertyAddress();
        if (primaryAddress != null) {
          primaryAddress.setPrimary(true);
          addresses.add(primaryAddress);
        }
        migrationMetadata.setAddresses(addressMapper.mapToDtoList(addresses));
      }
    }
    return repository.save(migrationMetadata);
  }
}
