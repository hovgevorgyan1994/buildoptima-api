package com.vecondev.buildoptima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class BuildoptimaApplication {

  public static void main(String[] args) {
    SpringApplication.run(BuildoptimaApplication.class, args);
  }
}
