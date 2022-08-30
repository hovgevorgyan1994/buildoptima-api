package com.vecondev.buildoptima.config;

import com.vecondev.buildoptima.config.properties.OpenSearchConfigProperties;
import com.vecondev.buildoptima.exception.Error;
import com.vecondev.buildoptima.exception.OpenSearchException;
import com.vecondev.buildoptima.util.FileReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OpenSearchConfig {

  private final OpenSearchConfigProperties properties;

  @Bean
  public CredentialsProvider credentialsProvider() {
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(
        AuthScope.ANY,
        new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword()));
    log.info("Created new BasicCredentialsProvider to access AWS OpenSearch cluster");
    return credentialsProvider;
  }

  @Bean
  public RestClientBuilder restClientBuilder(CredentialsProvider credentialsProvider) {
    RestClientBuilder builder =
        RestClient.builder(
                new HttpHost(
                    properties.getHostname(), properties.getPort(), properties.getSchema()))
            .setHttpClientConfigCallback(
                httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
    log.info("Created new RestClientBuilder for AWS OpenSearch cluster");
    return builder;
  }

  @Bean
  public RestHighLevelClient restHighLevelClient(RestClientBuilder builder) {
    RestHighLevelClient client = new RestHighLevelClient(builder);
    try {
      log.info("Created new RestHighLevelClient to access AWS OpenSearch cluster");

      GetIndexRequest getIndexRequest = new GetIndexRequest(properties.getIndexName());
      if (!client.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(properties.getIndexName());
        createIndexRequest.mapping(
            (String) FileReader.readFromJson(properties.getIndexMappingPath()), XContentType.JSON);
        createIndexRequest.settings(
            (String) FileReader.readFromJson(properties.getIndexSettingsPath()), XContentType.JSON);
        client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        log.info("Created new index named {} in AWS OpenSearch cluster", properties.getIndexName());
      }
      return client;
    } catch (Exception e) {
      throw new OpenSearchException(Error.FAILED_INDEX_CREATION);
    }
  }
}
