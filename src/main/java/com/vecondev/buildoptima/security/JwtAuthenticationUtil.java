package com.vecondev.buildoptima.security;

import com.vecondev.buildoptima.manager.CertificateManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationUtil {

  private final CertificateManager certificateManager;
  private final JwtConfigProperties jwtConfigProperties;

  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  public String generateAccessToken(Authentication authResult) {
    return Jwts.builder()
        .setSubject(authResult.getName())
        .claim("authorities", authResult.getAuthorities())
        .claim("issuer", jwtConfigProperties.getIssuer())
        .setExpiration(accessTokenExpiration())
        .signWith(SignatureAlgorithm.HS512, String.valueOf(certificateManager.privateKey()))
        .compact();
  }

  public boolean isTokenValid(String authToken, String username) {
    String usernameFromToken = getUsernameFromToken(authToken);
    return usernameFromToken.equals(username);
  }

  public boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.after(new Date());
  }

  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parser()
        .setSigningKey(String.valueOf(certificateManager.publicKey()))
        .parseClaimsJws(token)
        .getBody();
  }

  private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Date accessTokenExpiration() {
    return java.sql.Date.valueOf(
        LocalDate.now()
            .plus(jwtConfigProperties.getAccessToken().getValidity(), ChronoUnit.DAYS));
  }

  private Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }
}
