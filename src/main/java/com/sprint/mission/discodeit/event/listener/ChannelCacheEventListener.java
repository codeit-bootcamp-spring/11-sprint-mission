package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelCacheEventListener {

  private final CacheManager cacheManager;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePrivateChannelCreated(PrivateChannelCreatedEvent event) {
    Cache cache = cacheManager.getCache("channels");
    if (cache == null) {
      log.warn("channels 캐시를 찾을 수 없음");
      return;
    }

    event.participantIds().forEach(userId -> {
      cache.evict(userId);
      log.debug("Private 채널 생성으로 인한 캐시 삭제 - userId: {}", userId);
    });
  }
}
