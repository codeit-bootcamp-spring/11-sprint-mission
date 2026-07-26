package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseRequiredEventListener {

  private final SseService sseService;

  @TransactionalEventListener
  public void on(NotificationCreatedEvent event) {
    NotificationDto.Response notification = event.notification();

    sseService.send(Set.of(notification.receiverId()), "notifications.created", notification);
    log.debug("SSE 알림 전송: receiverId={}", notification.receiverId());
  }

  @TransactionalEventListener
  public void on(BinaryContentUpdatedEvent event) {
    BinaryContentDto.Response binaryContent = event.binaryContent();

    sseService.broadcast("binaryContents.updated", binaryContent);
    log.debug("SSE 파일 상태 전송: binaryContentId={}, status={}",
        binaryContent.id(), binaryContent.status());
  }

  @TransactionalEventListener
  public void on(ChannelCreatedEvent event) {
    dispatch("channels.created", event.channel());
  }

  @TransactionalEventListener
  public void on(ChannelUpdatedEvent event) {
    dispatch("channels.updated", event.channel());
  }

  @TransactionalEventListener
  public void on(ChannelDeletedEvent event) {
    dispatch("channels.deleted", event.channel());
  }

  private void dispatch(String eventName, ChannelDto.Response channel) {
    if (channel.type() == ChannelType.PUBLIC) {
      sseService.broadcast(eventName, channel);
      log.debug("SSE 채널 이벤트 전체 전송: eventName={}, channelId={}", eventName, channel.id());
      return;
    }

    Set<UUID> receiverIds = channel.participants().stream()
        .map(UserDto.Response::id)
        .collect(Collectors.toSet());

    sseService.send(receiverIds, eventName, channel);
    log.debug("SSE 채널 이벤트 참여자 전송: eventName={}, channelId={}, 수신자={}명",
        eventName, channel.id(), receiverIds.size());
  }

  @TransactionalEventListener
  public void on(UserCreatedEvent event) {
    sseService.broadcast("users.created", event.user());
    log.debug("SSE 사용자 생성 전송: userId={}", event.user().id());
  }

  @TransactionalEventListener(fallbackExecution = true)
  public void on(UserUpdatedEvent event) {
    sseService.broadcast("users.updated", event.user());
    log.debug("SSE 사용자 수정 전송: userId={}", event.user().id());
  }

  @TransactionalEventListener
  public void on(UserDeletedEvent event) {
    sseService.broadcast("users.deleted", event.user());
    log.debug("SSE 사용자 삭제 전송: userId={}", event.user().id());
  }
}