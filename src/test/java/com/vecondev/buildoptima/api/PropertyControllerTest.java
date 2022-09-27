package com.vecondev.buildoptima.api;

import static com.vecondev.buildoptima.exception.Error.PROPERTY_NOT_FOUND;
import static com.vecondev.buildoptima.filter.model.PropertySearchCriteria.ADDRESS;
import static com.vecondev.buildoptima.filter.model.PropertySearchCriteria.AIN;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.services.s3.AmazonS3;
import com.vecondev.buildoptima.actions.PropertyResultActions;
import com.vecondev.buildoptima.config.AmazonS3Config;
import com.vecondev.buildoptima.config.properties.OpenSearchConfigProperties;
import com.vecondev.buildoptima.config.properties.S3ConfigProperties;
import com.vecondev.buildoptima.endpoints.PropertyEndpointUris;
import com.vecondev.buildoptima.exception.ResourceNotFoundException;
import com.vecondev.buildoptima.model.property.Address;
import com.vecondev.buildoptima.model.property.Property;
import com.vecondev.buildoptima.parameters.property.PropertyControllerTestParameters;
import com.vecondev.buildoptima.repository.property.AddressRepository;
import com.vecondev.buildoptima.repository.property.MigrationHistoryRepository;
import com.vecondev.buildoptima.repository.property.MigrationMetadataRepository;
import com.vecondev.buildoptima.repository.property.PropertyRepository;
import com.vecondev.buildoptima.repository.user.UserRepository;
import com.vecondev.buildoptima.service.property.PropertyMigrationService;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.reindex.DeleteByQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
@Import({
  PropertyEndpointUris.class,
  PropertyResultActions.class,
  AmazonS3Config.class
})
class PropertyControllerTest {

  private static final String[] TEST_FILES = {"100.json.gz", "101.json.gz", "invalid.json"};
  private final PropertyControllerTestParameters testParameters =
      new PropertyControllerTestParameters();

  @Autowired private PropertyMigrationService propertyMigrationService;
  @Autowired private AmazonS3 amazonS3;
  @Autowired private UserRepository userRepository;
  @Autowired private PropertyRepository propertyRepository;
  @Autowired private AddressRepository addressRepository;
  @Autowired private MigrationMetadataRepository migrationMetadataRepository;
  @Autowired private MigrationHistoryRepository migrationHistoryRepository;
  @Autowired private RestHighLevelClient restHighLevelClient;
  @Autowired private OpenSearchConfigProperties openSearchConfigProperties;
  @Autowired private S3ConfigProperties s3ConfigProperties;
  @Autowired private PropertyResultActions propertyResultActions;

  @BeforeEach
  void setUp() {
    Arrays.stream(TEST_FILES)
        .forEach(
            file ->
                amazonS3.putObject(
                    s3ConfigProperties.getDataBucketName(), file, testParameters.getFile(file)));
    propertyMigrationService.migrateFromS3();
    userRepository.saveAll(testParameters.users());
  }

  @AfterEach
  void tearDown() throws IOException {
    Arrays.stream(TEST_FILES)
        .forEach(
            file -> {
              if (amazonS3.doesObjectExist(s3ConfigProperties.getDataBucketName(), file)) {
                amazonS3.deleteObject(s3ConfigProperties.getDataBucketName(), file);
              }
            });
    restHighLevelClient.deleteByQuery(
        new DeleteByQueryRequest(openSearchConfigProperties.getIndexName())
            .setQuery(QueryBuilders.matchAllQuery()),
        RequestOptions.DEFAULT);
    userRepository.deleteAll();
    propertyRepository.deleteAll();
    addressRepository.deleteAll();
    migrationMetadataRepository.deleteAll();
    migrationHistoryRepository.deleteAll();
  }

  @Test
  void getByAinSuccess() throws Exception {
    Property property =
        propertyRepository.findAll().stream()
            .findAny()
            .orElseThrow(() -> new ResourceNotFoundException(PROPERTY_NOT_FOUND));

    propertyResultActions
        .getByAin(property.getAin())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ain").value(property.getAin()))
        .andExpect(jsonPath("$.municipality").value(property.getMunicipality()));
  }

  @Test
  void getByAinFailedAsPropertyNotFound() throws Exception {
    String ain = String.valueOf(new Random().nextInt());
    assumeFalse(propertyRepository.existsByAin(ain));

    propertyResultActions.getByAin(ain).andExpect(status().isNotFound());
  }

  @Test
  void successfulSearchByAddress() throws Exception {
    List<Address> addresses =
        propertyRepository.findAll().stream()
            .findAny()
            .orElseThrow(() -> new ResourceNotFoundException(PROPERTY_NOT_FOUND))
            .getAddresses();
    assumeFalse(addresses.isEmpty());
    Address address = addresses.stream().findAny().orElseThrow();

    propertyResultActions
        .search(address.getHouseNumber(), ADDRESS)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty());
  }

  @Test
  void successfulSearchByAin() throws Exception {
    Property property =
        propertyRepository.findAll().stream()
            .findAny()
            .orElseThrow(() -> new ResourceNotFoundException(PROPERTY_NOT_FOUND));
    assumeFalse(property.getAddresses().isEmpty());

    propertyResultActions
        .search(property.getAin(), AIN)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isNotEmpty());
  }
}
