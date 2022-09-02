package com.vecondev.buildoptima.endpoints;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;

@Setter
@TestConfiguration
@ConfigurationProperties(prefix = "config.uris.property")
public class PropertyEndpointUris implements EndpointUris {

  private String baseUri;
  private String findByAinUri;
  private String searchUri;

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

  public String getFindByAinUri() {
    return baseUri + findByAinUri;
  }

  public String getSearchUri() {
    return baseUri + searchUri;
  }
}
