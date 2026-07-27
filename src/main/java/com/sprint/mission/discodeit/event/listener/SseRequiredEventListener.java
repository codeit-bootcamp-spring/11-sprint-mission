package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.channel.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.notification.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserDeletedEvent;
import com.sprint.mission.discodeit.event.user.UserLogInOutEvent;
import com.sprint.mission.discodeit.event.user.UserUpdatedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.sse.service.SseService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
public class SseRequiredEventListener {

  private final SseService sseService;
  private final ReadStatusRepository readStatusRepository;

  // Notification
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(NotificationCreatedEvent event) {

    NotificationDto dto = event.getData();

    sseService.send(List.of(dto.receiverId()), "notifications.created", dto);
  }


  // BinaryContent
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentUpdatedEvent event) {

    BinaryContentDto dto = event.getTo();

    // receiverId가 존재할 때 → UserService의 create, update 메서드의 프로필 이미지
    if (event.getReceiverId() != null) {
      sseService.send(List.of(event.getReceiverId()), "binaryContents.updated", dto);
    }
    // channelId가 존재할 때 → MessageService의 create 메서드의 첨부파일
    else if (event.getChannelId() != null) {
      List<UUID> receiverIds = readStatusRepository.findByChannelId(event.getChannelId()).stream()
          .map(readStatus -> readStatus.getUser().getId())
          .toList();

      sseService.send(receiverIds, "binaryContents.updated", dto);
    }
  }


  // Channel
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelCreatedEvent event) {

    ChannelDto dto = event.getData();

    // 생성하려는 채널이 PUBLIC 채널이면
    if (event.getReceiverIds() == null) {
      sseService.broadcast("channels.created", dto);
    }
    // 생성하려는 채널이 PRIVATE 채널이면
    else {
      sseService.send(event.getReceiverIds(), "channels.created", dto);
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelUpdatedEvent event) {

    ChannelDto dto = event.getTo();

    sseService.broadcast("channels.updated", dto);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChannelDeletedEvent event) {

    ChannelDto dto = event.getData();

    // PUBLIC 채널
    if (event.getReceiverIds() == null) {
      sseService.broadcast("channels.deleted", dto);
    }
    // PRIVATE 채널
    else {
      sseService.send(event.getReceiverIds(), "channels.deleted", dto);
    }
  }


  // User
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserCreatedEvent event) {

    UserDto dto = event.getData();

    sseService.broadcast("users.created", dto);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserUpdatedEvent event) {

    UserDto dto = event.getTo();

    sseService.broadcast("users.updated", dto);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserDeletedEvent event) {

    UserDto dto = event.getData();

    sseService.broadcast("users.deleted", dto);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(UserLogInOutEvent event) {

    sseService.broadcast("users.updated", event);
  }

}
