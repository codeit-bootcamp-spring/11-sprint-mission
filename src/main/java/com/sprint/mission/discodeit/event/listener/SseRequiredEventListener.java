package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.event.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.event.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.UserCreatedEvent;
import com.sprint.mission.discodeit.event.UserDeletedEvent;
import com.sprint.mission.discodeit.event.UserUpdatedEvent;
import com.sprint.mission.discodeit.service.SseService;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SseRequiredEventListener {

  private static final String PRIVATE_TYPE = "PRIVATE";

  private final SseService sseService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(NotificationCreatedEvent event) {
    UUID receiverId = event.getData().receiverId();
    sseService.send(Set.of(receiverId), "notifications.created", event.getData());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentUpdatedEvent event) {
    UUID uploaderId = event.getUploaderId();
    if (uploaderId == null) {
      // 회원가입 시점 업로드 등, 알릴 대상이 없는 경우
      return;
    }
    sseService.send(Set.of(uploaderId), "binaryContents.updated", event.getData());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelCreatedEvent event) {
    broadcastOrSendToParticipants(event.getData(), "channels.created");
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelUpdatedEvent event) {
    broadcastOrSendToParticipants(event.getData(), "channels.updated");
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelDeletedEvent event) {
    broadcastOrSendToParticipants(event.getData(), "channels.deleted");
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserCreatedEvent event) {
    sseService.broadcast("users.created", event.getData());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserUpdatedEvent event) {
    sseService.broadcast("users.updated", event.getData());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserDeletedEvent event) {
    sseService.broadcast("users.deleted", event.getData());
  }

  private void broadcastOrSendToParticipants(ChannelDto channelDto, String eventName) {
    if (PRIVATE_TYPE.equals(channelDto.type())) {
      // PRIVATE 채널은 참여자 외 유저에게 존재 자체가 알려지면 안 되므로 참여자에게만 전송
      Set<UUID> participantIds = channelDto.participants().stream()
          .map(UserDto::id)
          .collect(java.util.stream.Collectors.toSet());
      sseService.send(participantIds, eventName, channelDto);
    } else {
      // PUBLIC 채널은 모든 유저의 채널 목록에 반영되어야 하므로 브로드캐스트
      sseService.broadcast(eventName, channelDto);
    }
  }
}