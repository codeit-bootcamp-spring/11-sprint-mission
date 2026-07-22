package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.PrivateChannelCreatedEvent;
import java.util.List;
import java.util.UUID;
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
public class PrivateChannelCreatedEventListener {

  private final CacheManager cacheManager;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onPrivateChannelCreated(PrivateChannelCreatedEvent event) {
    log.info("Private 채널 생성 이벤트 수신(커밋 완료 후) - channelId: {}", event.channelId());

    List<UUID> participantIds = event.participantIds();
    if (participantIds == null || participantIds.isEmpty()) {
      return;
    }

    Cache cache = cacheManager.getCache("channels");
    if (cache != null) {
      for (UUID userId : participantIds) {
        cache.evict(userId);
      }
      log.info("Private 채널 참여자 {}명의 channels 캐시 무효화 완료", participantIds.size());
    }
  }
}
