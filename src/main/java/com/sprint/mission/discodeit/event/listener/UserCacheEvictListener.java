package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.user.UserOnlineStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCacheEvictListener {

  private final CacheManager cacheManager;

  @EventListener
  public void handleUserOnlineStatusChanged(UserOnlineStatusChangedEvent event) {
    Cache cache = cacheManager.getCache("users");
    if (cache == null) {
      log.warn("users 캐시를 찾을 수 없음");
      return;
    }
    cache.evict("all");
    log.debug("유저 온라인 상태 변경으로 인한 users 캐시 삭제 - userId: {}, online: {}",
        event.userId(), event.online());
  }
}
