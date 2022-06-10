package com.vecondev.buildoptima.security;

import com.vecondev.buildoptima.error.AuthErrorCode;
import com.vecondev.buildoptima.exception.AuthException;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

  private final JwtConfigProperties jwtConfigProperties;
  private final JwtAuthenticationUtil jwtTokenUtil;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String requestHeader = request.getHeader(jwtConfigProperties.getAuthorizationHeader());

    String authToken =
        requestHeader.replace(jwtConfigProperties.getAuthorizationHeaderPrefix(), "");

    if (Strings.isBlank(requestHeader)
        || !requestHeader.startsWith(jwtConfigProperties.getAuthorizationHeaderPrefix())) {
      filterChain.doFilter(request, response);
    }

    if (jwtTokenUtil.isTokenExpired(authToken)) {
      throw new AuthException(AuthErrorCode.AUTH_ACCESS_TOKEN_EXPIRED);
    }

    try {

      String username = jwtTokenUtil.getUsernameFromToken(authToken);

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (!jwtTokenUtil.isTokenValid(authToken, userDetails.getUsername())) {
        filterChain.doFilter(request, response);
      }

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (JwtException e) {
      SecurityContextHolder.clearContext();
      throw new IllegalStateException(String.format("Token %s cannot be trusted", authToken));
    }

    filterChain.doFilter(request, response);
  }
}
