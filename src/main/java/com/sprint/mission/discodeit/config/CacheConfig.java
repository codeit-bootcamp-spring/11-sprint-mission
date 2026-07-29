package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

  public static final String CHANNELS = "channels";
  public static final String NOTIFICATIONS = "notifications";
  public static final String USERS = "users";

  @Bean
  public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
    ObjectMapper redisObjectMapper = objectMapper.copy();

    PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType("com.sprint.mission.discodeit")
        .allowIfSubType("java.")
        .allowIfSubTypeIsArray()
        .build();

    redisObjectMapper.activateDefaultTyping(
        validator,
        DefaultTyping.EVERYTHING,
        As.PROPERTY
    );

    return RedisCacheConfiguration.defaultCacheConfig()
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer(redisObjectMapper)
            )
        )
        .prefixCacheNameWith("discodeit:")
        .entryTtl(Duration.ofSeconds(600))
        .disableCachingNullValues();
  }

  @Bean("jwtRedisSerializer")
  public GenericJackson2JsonRedisSerializer jwtRedisSerializer(ObjectMapper objectMapper) {
    ObjectMapper redisObjectMapper = objectMapper.copy();
    PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType("com.sprint.mission.discodeit")
        .allowIfSubType("java.")
        .allowIfSubTypeIsArray()
        .build();
    redisObjectMapper.activateDefaultTyping(validator, DefaultTyping.EVERYTHING, As.PROPERTY);
    return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
  }

  @Bean
  public RedisTemplate<String, Object> jwtRedisTemplate(RedisConnectionFactory connectionFactory,
      @Qualifier("jwtRedisSerializer") GenericJackson2JsonRedisSerializer serializer) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashValueSerializer(serializer);
    template.afterPropertiesSet();
    return template;
  }
}
