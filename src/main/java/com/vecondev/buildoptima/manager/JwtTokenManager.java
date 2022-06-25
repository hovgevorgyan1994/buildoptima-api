package com.vecondev.buildoptima.manager;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vecondev.buildoptima.config.properties.JwtConfigProperties;
import com.vecondev.buildoptima.error.ErrorCode;
import com.vecondev.buildoptima.exception.AuthenticationException;
import com.vecondev.buildoptima.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenManager {

  private final CertificateManager certificateManager;
  private final JwtConfigProperties jwtConfigProperties;

  private JWTVerifier tokenVerifier;
  private Algorithm algorithm;
  private Integer accessTokenValidity;

  @PostConstruct
  private void init() {
    accessTokenValidity = jwtConfigProperties.getAccessToken().getValidity();
    algorithm = Algorithm.RSA256((RSAPrivateKey) certificateManager.privateKey());
    tokenVerifier =
        JWT.require(Algorithm.RSA256((RSAPublicKey) certificateManager.publicKey())).build();
  }

  public String generateAccessToken(User user) {
    log.info("Just created a new access token with {} minutes validity", accessTokenValidity);
    return JWT.create()
        .withSubject(user.getEmail())
        .withIssuedAt(Instant.now())
        .withExpiresAt(Instant.now().plus(accessTokenValidity, ChronoUnit.MINUTES))
        .sign(algorithm);
  }

  public void validateToken(String token) {
    try {
      tokenVerifier.verify(token);
      log.info("A valid access token was provided");
    } catch (TokenExpiredException ex) {
      log.warn("An expired access token was provided");
      throw new AuthenticationException(ErrorCode.ACCESS_TOKEN_EXPIRED);
    } catch (JWTVerificationException ex) {
      log.warn("An invalid access token was provided");
      throw new AuthenticationException(ErrorCode.ACCESS_TOKEN_MISSING);
    }
  }

  public String getUsernameFromToken(String token) {
    return decodeToken(token).getSubject();
  }

  private DecodedJWT decodeToken(String token) {
    try {
      DecodedJWT verify = tokenVerifier.verify(token);
      log.info("A valid access token was provided");
      return verify;
    } catch (TokenExpiredException ex) {
      log.warn("An expired access token was provided");
      throw new AuthenticationException(ErrorCode.ACCESS_TOKEN_EXPIRED);
    } catch (JWTVerificationException ex) {
      log.warn("An invalid access token was provided");
      throw new AuthenticationException(ErrorCode.ACCESS_TOKEN_MISSING);
    }
  }
}
