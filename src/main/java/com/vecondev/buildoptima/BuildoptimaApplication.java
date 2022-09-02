package com.vecondev.buildoptima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableConfigurationProperties
@EnableTransactionManagement
@EnableScheduling
public class BuildoptimaApplication {

  public static void main(String[] args) {
    SpringApplication.run(BuildoptimaApplication.class, args);
  }
}
