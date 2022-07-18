package com.vecondev.buildoptima.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "page")
public class FilterConfigProperties {

  @NotNull private int skip;
  @NotNull private int take;
}
