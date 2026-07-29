package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.dto.PrivateChannelCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelCacheEventListener {

  private final CacheManager cacheManager;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(PrivateChannelCreatedEvent event) {
    Cache cache = cacheManager.getCache("channels");

    if (cache != null) {
      event.participantIds().forEach(cache::evict);
      log.debug("프라이빗 채널 캐시 무효화 완료 - participantsIds: {}", event.participantIds());
    } else {
      log.warn("채널 캐시를 찾을 수 없습니다.");
    }
  }
}
