package com.sprint.mission.discodeit.event;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class PrivateChannelCreatedEventListener {

  private final CacheManager cacheManager;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(PrivateChannelCreatedEvent event) {
    log.debug("채널 캐시 무효화 시작: channelId={}, 참여자 수={}",
        event.channelId(), event.participantIds().size());
    Optional.ofNullable(cacheManager.getCache("channels"))
        .ifPresent(cache -> event.participantIds().forEach(cache::evict));
    log.info("채널 캐시 무효화 완료: channelId={}", event.channelId());
  }
}