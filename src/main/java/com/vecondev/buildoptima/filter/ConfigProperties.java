package com.vecondev.buildoptima.filter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "page")
public class ConfigProperties {

  @NotNull private int skip;
  @NotNull private int take;
}
