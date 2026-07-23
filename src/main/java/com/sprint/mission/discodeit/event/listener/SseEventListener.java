package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.event.message.*;
import com.sprint.mission.discodeit.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseEventListener {

    private final SseService sseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(NotificationCreatedEvent event) {
        NotificationDto notification = event.getData();
        sseService.send(Set.of(notification.receiverId()), "notifications.created", notification);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BinaryContentUpdatedEvent event) {
        BinaryContentDto binaryContent = event.getData();
        sseService.broadcast("binaryContents.updated", binaryContent);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelCreatedEvent event) {
        routeChannelEvent("channels.created", event.getData());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelUpdatedEvent event) {
        routeChannelEvent("channels.updated", event.getData());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelDeletedEvent event) {
        routeChannelEvent("channels.deleted", event.getData());
    }

    private void routeChannelEvent(String eventName, ChannelDto channel) {
        if (channel.type() == ChannelType.PUBLIC) {
            sseService.broadcast(eventName, channel);
        } else {
            Set<UUID> participantIds = channel.participants().stream()
                    .map(UserDto::id)
                    .collect(Collectors.toSet());
            sseService.send(participantIds, eventName, channel);
        }
    }

}
