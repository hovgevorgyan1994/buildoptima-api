package com.vecondev.buildoptima.manager;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vecondev.buildoptima.config.properties.JwtConfigProperties;
import com.vecondev.buildoptima.exception.AuthenticationException;
import com.vecondev.buildoptima.exception.Error;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenManager {

  private final CertificateManager certificateManager;
  private final JwtConfigProperties jwtConfigProperties;

  private JWTVerifier tokenVerifier;
  private Algorithm algorithm;
  private Integer accessTokenValidity;
  private static final String ID = "id";
  private static final String AUTHORITIES = "authorities";

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
            .withClaim(ID, user.getId().toString())
            .withClaim(AUTHORITIES, authoritiesAsString(user.getRole().getAuthorities()))
            .withIssuer(jwtConfigProperties.getIssuer())
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(accessTokenValidity, ChronoUnit.MINUTES))
            .sign(algorithm);
  }

  public AppUserDetails getUserDetailsFromToken(String token) {
    DecodedJWT decoded = validateToken(token);
    List<String> authorities = decoded.getClaim(AUTHORITIES).asList(String.class);
    return AppUserDetails.builder()
            .id(UUID.fromString(decoded.getClaim(ID).asString()))
            .username(decoded.getSubject())
            .authorities(authoritiesFromString(authorities))
            .build();
  }

  private List<String> authoritiesAsString(List<SimpleGrantedAuthority> authorities) {
    return authorities.stream().map(SimpleGrantedAuthority::getAuthority).toList();
  }

  private List<SimpleGrantedAuthority> authoritiesFromString(List<String> authorities) {
    return authorities.stream().map(SimpleGrantedAuthority::new).toList();
  }

  private DecodedJWT validateToken(String token) {
    try {
      DecodedJWT verify = tokenVerifier.verify(token);
      if (!verify.getIssuer().equals(jwtConfigProperties.getIssuer())) {
        throw new JWTVerificationException("");
      }
      log.info("A valid access token was provided");
      return verify;
    } catch (TokenExpiredException ex) {
      log.warn("An expired access token was provided");
      throw new AuthenticationException(Error.ACCESS_TOKEN_EXPIRED);
    } catch (JWTVerificationException ex) {
      log.warn("An invalid access token was provided");
      throw new AuthenticationException(Error.INVALID_ACCESS_TOKEN);
    }
  }
}
