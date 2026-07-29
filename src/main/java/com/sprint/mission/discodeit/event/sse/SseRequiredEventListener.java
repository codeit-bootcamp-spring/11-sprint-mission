package com.sprint.mission.discodeit.event.sse;

import com.sprint.mission.discodeit.event.binaryContent.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.notification.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserDeletedEvent;
import com.sprint.mission.discodeit.event.user.UserUpdatedEvent;
import com.sprint.mission.discodeit.service.SseService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SseRequiredEventListener {

  private final SseService sseService;

  @EventListener
  public void on(NotificationCreatedEvent event) {
    sseService.send(Set.of(event.data().receiverId()), "notifications.created", event.data());
  }

  @EventListener
  public void on(BinaryContentUpdatedEvent event) {
    sseService.send(Set.of(event.ownerId()), "binaryContents.updated", event.data());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelCreatedEvent event) {
    if (event.participantIds().isEmpty()) {
      // 퍼블릭 채널은 전체 공지
      sseService.broadcast("channels.created", event.data());
    } else {
      // 프라이빗 채널은 참여자에게만
      sseService.send(event.participantIds(), "channels.created", event.data());
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelUpdatedEvent event) {
    sseService.broadcast("channels.updated", event.data());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelDeletedEvent event) {
    if (event.participantIds().isEmpty()) {
      sseService.broadcast("channels.deleted", event.channelId());
    } else {
      sseService.send(event.participantIds(), "channels.deleted", event.channelId());
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserCreatedEvent event) {
    sseService.broadcast("users.created", event.data());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserUpdatedEvent event) {
    sseService.broadcast("users.updated", event.data());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserDeletedEvent event) {
    sseService.broadcast("users.deleted", event.userId());
  }

}
