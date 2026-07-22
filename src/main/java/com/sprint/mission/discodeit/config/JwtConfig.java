package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.jwt.InMemoryJwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  @Bean
  public JwtRegistry jwtRegistry(
      @Value("${discodeit.security.jwt.max-active-count:1}") int maxActiveJwtCount,
      ApplicationEventPublisher eventPublisher) {
    return new InMemoryJwtRegistry(maxActiveJwtCount, eventPublisher);
  }
}
