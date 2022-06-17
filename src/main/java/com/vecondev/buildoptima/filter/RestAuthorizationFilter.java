package com.vecondev.buildoptima.filter;

import com.vecondev.buildoptima.error.AuthErrorCode;
import com.vecondev.buildoptima.exception.AuthException;
import com.vecondev.buildoptima.security.JwtConfigProperties;
import com.vecondev.buildoptima.security.JwtTokenManager;
import com.vecondev.buildoptima.security.SecurityContextService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class RestAuthorizationFilter extends OncePerRequestFilter {

  private final JwtTokenManager jwtTokenManager;
  private final JwtConfigProperties jwtConfigProperties;
  private final SecurityContextService securityContextService;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if (StringUtils.isBlank(request.getHeader(jwtConfigProperties.getAuthorizationHeader()))) {
      filterChain.doFilter(request, response);
    }

    final String accessToken = getAccessToken(request);

    if (jwtTokenManager.isTokenExpired(accessToken)) {
      throw new AuthException(AuthErrorCode.AUTH_ACCESS_TOKEN_EXPIRED,AuthErrorCode.AUTH_ACCESS_TOKEN_EXPIRED.getMessage());
    }
    String username = jwtTokenManager.getUsernameFromToken(accessToken);

    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    if (!jwtTokenManager.isTokenValid(accessToken, userDetails.getUsername())) {
      filterChain.doFilter(request, response);
    }

    if (StringUtils.isNoneBlank(accessToken)) {
      log.info("Successfully parsed JWT token from headers. Trying to authenticate user.");
      setAuthentication(userDetails);
    } else {
      log.info(
          "No JWT token found in the headers for uri {} {}",
          request.getMethod(),
          request.getRequestURI());
    }
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return (request.getRequestURI().equals("/user")
        || request.getRequestURI().equals("/user/refreshToken")
        || request.getRequestURI().equals("/registration")
        || request.getRequestURI().equals("/registration/activate"));
  }

  @Override
  protected boolean shouldNotFilterErrorDispatch() {
    return true;
  }

  private void setAuthentication(UserDetails userDetails) {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    securityContextService.setAuthentication(authentication);
  }

  private String getAccessToken(HttpServletRequest request) {
    final String authBearerToken = request.getHeader(jwtConfigProperties.getAuthorizationHeader());
    return StringUtils.trimToNull(
        StringUtils.substringAfter(
            authBearerToken, jwtConfigProperties.getAuthorizationHeaderPrefix()));
  }
}
