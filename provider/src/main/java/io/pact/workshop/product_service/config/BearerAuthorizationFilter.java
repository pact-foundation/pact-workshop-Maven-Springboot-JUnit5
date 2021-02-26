package io.pact.workshop.product_service.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

public class BearerAuthorizationFilter extends OncePerRequestFilter {

  public static final long ONE_HOUR = 60 * 60 * 1000L;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (tokenValid(header)) {
      SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken("user", header));
      filterChain.doFilter(request, response);
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  private boolean tokenValid(String header) {
    boolean hasBearerToken = StringUtils.isNotEmpty(header) && header.startsWith("Bearer ");
    if (hasBearerToken) {
      String token = header.substring("Bearer ".length());
      ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
      buffer.put(Base64.getDecoder().decode(token));
      buffer.flip();
      long timestamp = buffer.getLong();
      return System.currentTimeMillis() - timestamp <= ONE_HOUR;
    } else {
      return false;
    }
  }
}
