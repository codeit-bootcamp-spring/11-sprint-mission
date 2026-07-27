package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  public RedisSerializer<Object> jwtRedisSerializer(ObjectMapper objectMapper) {
    ObjectMapper jwtObjectMapper = objectMapper.copy();

    PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType("com.sprint.mission.discodeit")
        .allowIfSubType("java.util")
        .build();

    jwtObjectMapper.activateDefaultTyping(
        typeValidator,
        DefaultTyping.EVERYTHING,
        As.PROPERTY
    );

    return new GenericJackson2JsonRedisSerializer(jwtObjectMapper);
  }

  @Bean
  public RedisTemplate<String, Object> jwtRedisTemplate(
      RedisConnectionFactory connectionFactory,
      RedisSerializer<Object> jwtRedisSerializer
  ) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(jwtRedisSerializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(jwtRedisSerializer);
    template.afterPropertiesSet();
    return template;
  }
}
