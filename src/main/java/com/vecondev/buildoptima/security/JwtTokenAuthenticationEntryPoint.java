package com.vecondev.buildoptima.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    if (response.getStatus() == 403) {
      log.warn("Sending 403 FORBIDDEN response, because the access token was expired");
      response.sendError(response.getStatus(), response.getCharacterEncoding());
    } else {
      log.error(
          "Sending 401 UNAUTHORIZED response, because the access token was invalid or was missing");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
  }
}
