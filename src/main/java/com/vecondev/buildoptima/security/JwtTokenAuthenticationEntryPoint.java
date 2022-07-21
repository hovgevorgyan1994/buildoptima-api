package com.vecondev.buildoptima.security;

import com.vecondev.buildoptima.exception.ApiError;
import com.vecondev.buildoptima.exception.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

import static com.vecondev.buildoptima.util.JsonUtil.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    String header = response.getHeader("error");
    Error error;
    if (Strings.isNotEmpty(header)) {
      error = Error.valueOf(header);
    } else {
      error = Error.ACCESS_TOKEN_MISSING;
    }
    sendResponse(error, response);
  }

  private void sendResponse(Error error, HttpServletResponse response) throws IOException {
    ApiError apiError =
        new ApiError(error.getHttpStatus(), error.getCode(), Instant.now(), error.getMessage());
    response.setContentType(APPLICATION_JSON_VALUE);
    response.setStatus(error.getHttpStatus().value());
    response.getWriter().write(writeToJson(apiError));
  }
}
