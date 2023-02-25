package io.pact.workshop.product_service.config;

import static java.util.regex.Pattern.matches;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class BearerAuthorizationFilter extends OncePerRequestFilter {

  public static final long ONE_HOUR = 60 * 60 * 1000L;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    final var header = request.getHeader("Authorization");
    if (noVerifyAuthorizationHeader(request.getRequestURI())) {
      filterChain.doFilter(request, response);
    } else if (tokenValid(header)) {
      SecurityContextHolder.getContext()
          .setAuthentication(new PreAuthenticatedAuthenticationToken("user", header));
      filterChain.doFilter(request, response);
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  private boolean tokenValid(String header) {
    final var hasBearerToken = StringUtils.isNotEmpty(header) && header.startsWith("Bearer ");
    if (hasBearerToken) {
      final var token = header.substring("Bearer ".length());
      final var buffer = ByteBuffer.allocate(Long.BYTES);
      buffer.put(Base64.getDecoder().decode(token));
      buffer.flip();
      final var timestamp = buffer.getLong();
      return System.currentTimeMillis() - timestamp <= ONE_HOUR;
    }
    return false;
  }

  private static boolean noVerifyAuthorizationHeader(final String uri) {
    return matches("^\\/h2-console.*|.*(.ico)$", uri);
  }
}
