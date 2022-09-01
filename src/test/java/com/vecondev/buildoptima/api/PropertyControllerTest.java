package com.vecondev.buildoptima.api;

import static com.vecondev.buildoptima.exception.Error.ACCESS_DENIED;
import static com.vecondev.buildoptima.model.user.Role.CLIENT;
import static com.vecondev.buildoptima.model.user.Role.MODERATOR;
import static org.junit.Assume.assumeNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.services.s3.AmazonS3;
import com.vecondev.buildoptima.config.AmazonS3Config;
import com.vecondev.buildoptima.config.properties.S3ConfigProperties;
import com.vecondev.buildoptima.model.property.migration.MigrationHistory;
import com.vecondev.buildoptima.model.user.Role;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.actions.PropertyResultActions;
import com.vecondev.buildoptima.parameters.endpoints.PropertyEndpointUris;
import com.vecondev.buildoptima.parameters.property.PropertyControllerTestParameters;
import com.vecondev.buildoptima.repository.property.AddressRepository;
import com.vecondev.buildoptima.repository.property.MigrationHistoryRepository;
import com.vecondev.buildoptima.repository.property.MigrationMetadataRepository;
import com.vecondev.buildoptima.repository.property.PropertyRepository;
import com.vecondev.buildoptima.repository.user.UserRepository;
import com.vecondev.buildoptima.service.property.PropertyService;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
@Import({PropertyEndpointUris.class, PropertyResultActions.class, AmazonS3Config.class})
class PropertyControllerTest {

  private static final String[] TEST_FILES = {"100.json.gz", "101.json.gz", "invalid.json"};
  private static final int PROPERTIES_PER_FILE = 500;
  private final PropertyControllerTestParameters testParameters =
      new PropertyControllerTestParameters();
  @Autowired private AmazonS3 amazonS3;
  @Autowired private PropertyService propertyService;
  @Autowired private S3ConfigProperties s3ConfigProperties;
  @Autowired private UserRepository userRepository;
  @Autowired private PropertyRepository propertyRepository;
  @Autowired private AddressRepository addressRepository;
  @Autowired private MigrationMetadataRepository migrationMetadataRepository;
  @Autowired private MigrationHistoryRepository migrationHistoryRepository;
  @Autowired private PropertyResultActions propertyResultActions;

  @BeforeEach
  void setUp() {
    Arrays.stream(TEST_FILES)
        .forEach(
            file ->
                amazonS3.putObject(
                    s3ConfigProperties.getDataBucketName(), file, testParameters.getFile(file)));
    userRepository.saveAll(testParameters.users());
  }

  @AfterEach
  void tearDown() {
    Arrays.stream(TEST_FILES)
        .forEach(
            file -> {
              if (amazonS3.doesObjectExist(s3ConfigProperties.getDataBucketName(), file)) {
                amazonS3.deleteObject(s3ConfigProperties.getDataBucketName(), file);
              }
            });
    userRepository.deleteAll();
    propertyRepository.deleteAll();
    addressRepository.deleteAll();
    migrationMetadataRepository.deleteAll();
    migrationHistoryRepository.deleteAll();
  }

  @Test
  void successfulPropertiesMigration() throws Exception {
    User moderator = getUserByRole(MODERATOR);
    assumeNotNull(moderator);

    propertyResultActions
        .migrationResultActions(moderator)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allProcessedFiles").value(TEST_FILES.length))
        .andExpect(jsonPath("$.allProcessedProperties").value(getProcessedPropertiesCount()));
  }

  @Test
  void successfulPropertiesMigrationWithNoNewFiles() throws Exception {
    User moderator = getUserByRole(MODERATOR);
    assumeNotNull(moderator);

    propertyService.migrateFromS3();
    assumeTrue(migrationHistoryRepository.count() == TEST_FILES.length);

    propertyResultActions
        .migrationResultActions(moderator)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allProcessedFiles").value(TEST_FILES.length))
        .andExpect(jsonPath("$.allProcessedProperties").value(getProcessedPropertiesCount()));
  }

  @Test
  void failedPropertiesMigrationAsPermissionDenied() throws Exception {
    User client = getUserByRole(CLIENT);
    assumeNotNull(client);

    propertyResultActions
        .migrationResultActions(client)
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value(ACCESS_DENIED.getCode()))
        .andExpect(jsonPath("$.message").value(ACCESS_DENIED.getMessage()))
        .andExpect(jsonPath("$.timestamp").isNotEmpty());
  }

  @Test
  void successfulReprocessOfFailedFiles() throws Exception {
    User moderator = getUserByRole(MODERATOR);
    assumeNotNull(moderator);

    migrationHistoryRepository.save(
        new MigrationHistory(
            UUID.randomUUID(), TEST_FILES[0], Instant.now(), Instant.now(), "Not in GZIP format."));
    int failedMigrationHistories = migrationHistoryRepository.findAllByFailedAtNotNull().size();

    propertyResultActions
        .reprocessResultActions(moderator)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allSuccessfullyReprocessedFiles").value(failedMigrationHistories));
  }

  @Test
  void successfulMigrationTracking() throws Exception {
    User moderator = getUserByRole(MODERATOR);
    assumeNotNull(moderator);

    propertyService.migrateFromS3();

    propertyResultActions
        .trackProgressResultActions(moderator)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allProcessedFiles").value(TEST_FILES.length))
        .andExpect(jsonPath("$.allProcessedProperties").value(getProcessedPropertiesCount()));
  }

  private User getUserByRole(Role role) {
    return userRepository.findByRole(role).orElse(null);
  }

  private int getProcessedPropertiesCount() {
    return migrationHistoryRepository.findAllByFailedAtIsNull().size() * PROPERTIES_PER_FILE;
  }
}