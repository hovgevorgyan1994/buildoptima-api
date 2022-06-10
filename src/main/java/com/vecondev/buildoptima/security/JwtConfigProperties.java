package com.vecondev.buildoptima.security;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "platform.security.jwt")
public class JwtConfigProperties {

  private String issuer;
  private String privateKeyPath;
  private String publicKeyPath;
  private String authorizationHeader;
  private String authorizationHeaderPrefix;

  private SignatureAlgorithm signatureAlgorithm;
  private RefreshTokenConfigProperties refreshToken;
  private AccessTokenConfigProperties accessToken;

  @Data
  public static class AccessTokenConfigProperties {

    private Integer validity;
  }

  @Data
  public static class RefreshTokenConfigProperties {

    private Integer validity;
  }
}
