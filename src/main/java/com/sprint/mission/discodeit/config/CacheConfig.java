package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class CacheConfig {

  @Bean
  public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
    // Redis ObjectMapper 전용으로 복사
    ObjectMapper redisObjectMapper = objectMapper.copy();

    // ex) com.example.demo 프로젝트의 Person 클래스에는 name, age가 있음
    // Person person = new Person...으로 name은 "홍길동", age는 20일 경우
    // {"@class": "com.example.demo.Person", "name": "홍길동", "age": 20} 형태로 직렬화됨
    redisObjectMapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance, // 모든 타입에 역직렬화 허용
        DefaultTyping.EVERYTHING, // 모든 객체에 타입 정보 "@class"를 추가
        As.PROPERTY // 타입 정보("@class")를 Json 속성으로 저장
    );

    return RedisCacheConfiguration.defaultCacheConfig()
        // Value를 JSON으로 저장(직렬화)
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer(redisObjectMapper)
            )
        )
        // Redis Cache 이름에 접두사 추가
        // ex) userChannels 캐시 데이터일 경우 : Cacheable(value : userChannels, key = "#userId")
        // discodeit:userChannels::{userId}
        .prefixCacheNameWith("discodeit:")

        // 10분마다 캐시 자동 삭제
        .entryTtl(Duration.ofSeconds(600))

        // 조회 결과가 null인 경우 Redis에 저장하지 않음
        .disableCachingNullValues();
  }

}
