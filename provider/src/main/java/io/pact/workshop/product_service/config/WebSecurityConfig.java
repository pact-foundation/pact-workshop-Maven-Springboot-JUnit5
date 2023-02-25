package io.pact.workshop.product_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .addFilterBefore(
            new BearerAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
        .headers()
        .frameOptions()
        .sameOrigin();
    return http.build();
  }
}
