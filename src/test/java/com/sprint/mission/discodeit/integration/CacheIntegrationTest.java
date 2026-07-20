package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@WithMockUser(roles = "CHANNEL_MANAGER")
class CacheIntegrationTest {

  @Autowired
  private UserService userService;

  @Autowired
  private ChannelService channelService;

  @Autowired
  private CacheManager cacheManager;

  @BeforeEach
  void setUp() {
    cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
  }

  @Test
  @DisplayName("사용자 목록 조회 결과가 캐시되고, 사용자 추가 시 무효화된다")
  void findAllUsers_IsCachedAndEvictedOnCreate() {
    Cache users = cacheManager.getCache("users");
    assertThat(users).isNotNull();
    
    userService.findAll();
    
    assertThat(users.get(SimpleKey.EMPTY)).isNotNull();

    createUser();

    assertThat(users.get(SimpleKey.EMPTY)).isNull();
  }

  @Test
  @DisplayName("사용자별 채널 목록 조회 결과가 캐시되고, 채널 추가 시 무효화된다")
  void findAllChannelsByUserId_IsCachedAndEvictedOnChannelCreate() {
    Cache channels = cacheManager.getCache("channels");
    assertThat(channels).isNotNull();
    UUID userId = createUser().id();

    channelService.findAllByUserId(userId);
    
    assertThat(channels.get(userId)).isNotNull();
    
    channelService.create(new PublicChannelCreateRequest("새 채널", "설명"));
    
    assertThat(channels.get(userId)).isNull();
  }

  private UserDto createUser() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    return userService.create(
        new UserCreateRequest("cache" + suffix, "cache" + suffix + "@example.com", "Password1!"),
        Optional.empty());
  }
}