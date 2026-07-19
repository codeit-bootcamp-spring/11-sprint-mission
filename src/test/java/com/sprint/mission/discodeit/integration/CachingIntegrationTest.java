package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Cacheable / @CacheEvict가 실제 Spring 컨텍스트에서 의도한 대로 동작하는지 확인하는 통합 테스트입니다.
 * 순수 Mockito 단위 테스트로는 AOP 프록시가 적용되지 않아 캐시 동작 자체를 검증할 수 없으므로,
 * CacheManager를 직접 조회해 캐시 적재/무효화 여부를 확인합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CachingIntegrationTest {

  @Autowired
  private UserService userService;

  @Autowired
  private ChannelService channelService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private CacheManager cacheManager;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("사용자 목록 조회는 캐시되고, 사용자 생성 시 캐시가 무효화된다")
  void findAll_IsCached_AndEvictedOnCreate() {
    // given
    Cache allUsersCache = cacheManager.getCache(CacheConfig.ALL_USERS_CACHE);
    assertThat(allUsersCache).isNotNull();

    // when - 최초 조회로 캐시 적재
    List<UserDto> firstCall = userService.findAll();

    // then - 무인자 메소드는 SimpleKey.EMPTY로 캐시된다
    assertThat(allUsersCache.get(SimpleKey.EMPTY)).isNotNull();
    assertThat(allUsersCache.get(SimpleKey.EMPTY).get()).isEqualTo(firstCall);

    // when - 사용자 생성
    userService.create(
        new UserCreateRequest("cacheuser", "cacheuser@example.com", "Password1!"),
        Optional.empty());

    // then - 캐시가 무효화되어 더 이상 값이 없어야 한다
    assertThat(allUsersCache.get(SimpleKey.EMPTY)).isNull();
  }

  @Test
  @DisplayName("사용자별 채널 목록 조회는 사용자 단위로 캐시되고, 새 채널 생성 시 무효화된다")
  @WithMockUser(roles = "CHANNEL_MANAGER")
  void findAllByUserId_IsCached_AndEvictedOnChannelCreate() {
    // given
    UUID userId = userService.create(
        new UserCreateRequest("channelcacheuser", "channelcacheuser@example.com", "Password1!"),
        Optional.empty()
    ).id();

    Cache channelsByUserCache = cacheManager.getCache(CacheConfig.CHANNELS_BY_USER_CACHE);
    assertThat(channelsByUserCache).isNotNull();

    // when
    List<ChannelDto> firstCall = channelService.findAllByUserId(userId);

    // then
    assertThat(channelsByUserCache.get(userId)).isNotNull();
    assertThat(channelsByUserCache.get(userId).get()).isEqualTo(firstCall);

    // when - 새로운 공개 채널 생성(전체 캐시 무효화 대상)
    channelService.create(new PublicChannelCreateRequest("캐시 테스트 채널", "설명"));

    // then
    assertThat(channelsByUserCache.get(userId)).isNull();
  }

  @Test
  @DisplayName("사용자별 알림 목록 조회는 사용자 단위로 캐시되고, 알림 생성/삭제 시 무효화된다")
  void findAllByReceiverId_IsCached_AndEvictedOnCreateAndDelete() {
    // given
    UserDto receiver = userService.create(
        new UserCreateRequest("notificationcacheuser", "notificationcacheuser@example.com",
            "Password1!"),
        Optional.empty()
    );
    UUID receiverId = receiver.id();

    // delete()의 @PreAuthorize(principal.userDto.id == receiverId) 통과를 위해 본인으로 인증
    DiscodeitUserDetails receiverDetails = new DiscodeitUserDetails(receiver, "Password1!");
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            receiverDetails, null, receiverDetails.getAuthorities()));

    Cache notificationsByUserCache = cacheManager.getCache(
        CacheConfig.NOTIFICATIONS_BY_USER_CACHE);
    assertThat(notificationsByUserCache).isNotNull();

    // when - 최초 조회(빈 목록)로 캐시 적재
    List<NotificationDto> firstCall = notificationService.findAllByReceiverId(receiverId);
    assertThat(notificationsByUserCache.get(receiverId)).isNotNull();
    assertThat(notificationsByUserCache.get(receiverId).get()).isEqualTo(firstCall);

    // when - 알림 생성 시 해당 사용자 캐시만 무효화
    NotificationDto notification = notificationService.create(receiverId, "제목", "내용");
    assertThat(notificationsByUserCache.get(receiverId)).isNull();

    // given - 다시 조회해 캐시 재적재
    notificationService.findAllByReceiverId(receiverId);
    assertThat(notificationsByUserCache.get(receiverId)).isNotNull();

    // when - 알림 삭제 시에도 무효화
    notificationService.delete(notification.id());

    // then
    assertThat(notificationsByUserCache.get(receiverId)).isNull();
  }
}
