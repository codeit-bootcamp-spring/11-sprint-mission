package com.sprint.mission.discodeit.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * 사용자별 채널 목록/알림 목록/사용자 목록 조회에 Caffeine 캐시를 적용하기 위한 설정입니다.
 * 실제 캐시(CacheManager) Bean은 spring-boot-starter-cache의 자동 설정이 application.yaml의
 * spring.cache.type=caffeine, spring.cache.caffeine.spec 설정을 읽어 구성합니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

  public static final String ALL_USERS_CACHE = "allUsers";
  public static final String CHANNELS_BY_USER_CACHE = "channelsByUser";
  public static final String NOTIFICATIONS_BY_USER_CACHE = "notificationsByUser";
}
