package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.InMemoryJwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class jwtConfig {


  @Bean
  public JwtRegistry jwtRegistry(
      @Value("${discodeit.jwt.max-active-count:1}") int maxActiveJwtCount,
      JwtTokenProvider jwtTokenProvider
  ) {
    return new InMemoryJwtRegistry(maxActiveJwtCount, jwtTokenProvider);
  }


  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(
      JwtTokenProvider jwtTokenProvider,
      DiscodeitUserDetailsService userDetailsService,
      JwtRegistry jwtRegistry
  ) {
    return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, jwtRegistry);
  }

}
