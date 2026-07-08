package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.jwt.InMemoryJwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  @Bean
  public JwtRegistry jwtRegistry() {
    return new InMemoryJwtRegistry(1);
  }
}
