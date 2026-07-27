package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.RedisJwtRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class JwtConfig {

  @Bean
  public JwtRegistry jwtRegistry(
      JwtTokenProvider jwtTokenProvider,
      RedisTemplate<String, Object> redisTemplate,
      RedisLockProvider redisLockProvider) {
    return new RedisJwtRegistry(1, jwtTokenProvider, redisTemplate, redisLockProvider);
  }
}