package com.vecondev.buildoptima.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.vecondev.buildoptima.config.properties.SqsConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class SqsConfig {

  private final SqsConfigProperties configProperties;

  @Bean
  @Primary
  public AmazonSQSAsync amazonSqsAsync() {
    return AmazonSQSAsyncClientBuilder.standard()
        .withEndpointConfiguration(
            new AwsClientBuilder.EndpointConfiguration(
                configProperties.getUrl(), Regions.US_EAST_1.getName()))
        .withCredentials(
            new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(
                    configProperties.getAccessKey(), configProperties.getSecretKey())))
        .build();
  }
}
