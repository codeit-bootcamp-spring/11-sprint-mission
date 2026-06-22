package com.sprint.mission.discodeit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    SecurityFilterChain chain = http.build();

    log.info("========== Registered Security Filters ==========");
    chain.getFilters().forEach(filter ->
        log.info("Filter: {}", filter.getClass().getSimpleName())
    );
    log.info("=================================================");

    return chain;
  }
}
