package com.vecondev.buildoptima.security;

import com.vecondev.buildoptima.config.properties.JwtConfigProperties;
import com.vecondev.buildoptima.exception.AuthenticationException;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    try {
      Optional<String> accessToken = parseAccessToken(request);
      if (accessToken.isPresent()) {
        jwtTokenManager.validateToken(accessToken.get());
        String username = jwtTokenManager.getUsernameFromToken(accessToken.get());
        AppUserDetails appUser = (AppUserDetails) userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken upt =
            new UsernamePasswordAuthenticationToken(appUser, null, appUser.getAuthorities());
        upt.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(upt);
      }
    } catch (AuthenticationException ex) {
      response.addHeader("error", ex.getError().name());
      response.setStatus(ex.getError().getHttpStatus().value());
    } finally {
      filterChain.doFilter(request, response);
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
