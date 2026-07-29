package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.dto.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.event.dto.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.dto.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.dto.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.dto.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.dto.UserCreatedEvent;
import com.sprint.mission.discodeit.event.dto.UserDeletedEvent;
import com.sprint.mission.discodeit.event.dto.UserUpdatedEvent;
import com.sprint.mission.discodeit.service.SseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseRequiredEventListener {

  private final SseService sseService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(NotificationCreatedEvent event) {
    log.debug("SSE 리스너 알림 생성 이벤트 - receiverId: {}", event.data().receiverId());

    sseService.send(
        List.of(event.data().receiverId()),
        "notifications.created",
        event.data()
    );
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentUpdatedEvent event) {
    log.debug("SSE 리스너 파일 상태 변경 이벤트 - uploaderId: {}", event.uploaderId());

    sseService.send(
        List.of(event.uploaderId()),
        "binaryContents.updated",
        event.data()
    );
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelCreatedEvent event) {
    log.debug("SSE 리스너 채널 생성 브로드캐스트 - channelId: {}", event.data().id());

    sseService.broadcast("channels.created", event.data());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelUpdatedEvent event) {
    log.debug("SSE 리스너 채널 수정 브로드캐스트 - channelId: {}", event.data().id());

    sseService.broadcast("channels.updated", event.data());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelDeletedEvent event) {
    log.debug("SSE 리스너 채널 삭제 브로드캐스트 - channelId: {}", event.data().id());

    sseService.broadcast("channels.deleted", event.data());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserCreatedEvent event) {
    log.debug("SSE 리스너 유저 상태 생성 브로드캐스트 - userId: {}", event.data().id());

    sseService.broadcast("users.updated", event.data());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserUpdatedEvent event) {
    log.debug("SSE 리스너 유저 상태 수정 브로드캐스트 - userId: {}", event.data().id());

    sseService.broadcast("users.updated", event.data());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserDeletedEvent event) {
    log.debug("SSE 리스너 유저 상태 삭제 브로드캐스트 - userId: {}", event.data().id());

    sseService.broadcast("users.updated", event.data());
  }
}
