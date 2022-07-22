package com.vecondev.buildoptima.security;

import com.vecondev.buildoptima.config.properties.JwtConfigProperties;
import com.vecondev.buildoptima.exception.AuthenticationException;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.auth.SecurityContextService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAuthorizationFilter extends OncePerRequestFilter {

  private final JwtTokenManager jwtTokenManager;
  private final JwtConfigProperties jwtConfigProperties;
  private final SecurityContextService securityContextService;

  @Override
  protected void doFilterInternal(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain)
          throws ServletException, IOException {

    try {
      Optional<String> accessToken = parseAccessToken(request);
      if (accessToken.isPresent()) {
        AppUserDetails userDetails = jwtTokenManager.getUserDetailsFromToken(accessToken.get());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
        securityContextService.setAuthentication(authenticationToken);
      }
      filterChain.doFilter(request, response);
    } catch (AuthenticationException ex) {
      response.addHeader("error", ex.getError().name());
      response.setStatus(ex.getError().getHttpStatus().value());
    } finally {
      securityContextService.clearAuthentication();
    }
  }

  private Optional<String> parseAccessToken(HttpServletRequest request) {
    String authHeader = jwtConfigProperties.getAuthorizationHeader();
    String headerPrefix = jwtConfigProperties.getAuthorizationHeaderPrefix();
    String accessToken = request.getHeader(authHeader);
    if (StringUtils.hasText(accessToken) && accessToken.startsWith(headerPrefix)) {
      return Optional.of(accessToken.replace(headerPrefix, ""));
    }
    return Optional.empty();
  }
}
