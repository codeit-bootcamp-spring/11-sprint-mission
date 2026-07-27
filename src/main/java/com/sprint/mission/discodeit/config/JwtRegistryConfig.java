package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.security.jwt.InMemoryJwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.RedisJwtRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class JwtRegistryConfig {

  @Bean
  @Profile("dev")
  public JwtRegistry inMemoryJwtRegistry(
      @Value("${discodeit.security.jwt.max-active-count}") int maxActiveJwtCount,
      ApplicationEventPublisher eventPublisher
  ) {
    return new InMemoryJwtRegistry(maxActiveJwtCount, eventPublisher);
  }

  @Bean
  @Profile("prod")
  public JwtRegistry redisJwtRegistry(
      @Value("${discodeit.security.jwt.max-active-count}") int maxActiveJwtCount,
      ApplicationEventPublisher eventPublisher,
      RedisTemplate<String, Object> jwtRedisTemplate,
      RedisLockProvider redisLockProvider
  ) {
    return new RedisJwtRegistry(maxActiveJwtCount, eventPublisher, jwtRedisTemplate,
        redisLockProvider);
  }
}