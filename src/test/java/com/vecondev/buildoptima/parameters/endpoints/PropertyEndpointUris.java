package com.vecondev.buildoptima.parameters.endpoints;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;

@Setter
@TestConfiguration
@ConfigurationProperties(prefix = "config.uris.property")
public class PropertyEndpointUris implements EndpointUris {

  private String baseUri;
  private String migrationUri;
  private String reprocessUri;
  private String trackProgressUri;

  @Override
  public String getDeleteByIdUri() {
    return null;
  }

  @Override
  public String getUpdateUri() {
    return null;
  }

  @Override
  public String getCreationUri() {
    return null;
  }

  @Override
  public String getFetchUri() {
    return null;
  }

  @Override
  public String getRetrieveByIdUri() {
    return null;
  }

  @Override
  public String getAllUri() {
    return null;
  }

  @Override
  public String getExportInCsvUri() {
    return null;
  }

  @Override
  public String getMetadataUri() {
    return null;
  }

  public String getMigrationUri() {
    return baseUri + migrationUri;
  }

  public String getReprocessUri() {
    return baseUri + reprocessUri;
  }

  public String getTrackProgressUri() {
    return baseUri + trackProgressUri;
  }
}
