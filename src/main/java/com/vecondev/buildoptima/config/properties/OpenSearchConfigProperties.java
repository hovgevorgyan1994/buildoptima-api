package com.vecondev.buildoptima.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "opensearch.properties")
public class OpenSearchConfigProperties {

  private String username;
  private String password;
  private String hostname;
  private Integer port;
  private String schema;
  private String indexName;
  private String requestBodiesPath;
  private String indexMappingFileName;
  private String indexSettingsFileName;
}
