package com.vecondev.buildoptima.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Data
@TestConfiguration
@ConfigurationProperties(prefix = "config.aws.s3")
public class AmazonS3Config {

  private String region;
  private String url;
  private String imageBucketName;
  private String dataBucketName;
  private String accessKey;
  private String secretKey;

  @Bean
  @Profile("test")
  AmazonS3 amazonS3() {
    AmazonS3 amazonS3 =
        AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url, region))
            .withPathStyleAccessEnabled(true)
            .withCredentials(
                new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .build();

    amazonS3.createBucket(imageBucketName);
    amazonS3.createBucket(dataBucketName);

    return amazonS3;
  }
}
