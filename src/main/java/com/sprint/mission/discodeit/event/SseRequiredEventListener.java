package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.event.kafka.RealtimeEvent;
import com.sprint.mission.discodeit.event.kafka.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SseRequiredEventListener {

    private final RealtimeEventPublisher realtimeEventPublisher;

    @TransactionalEventListener(fallbackExecution = true)
    public void on(NotificationCreatedEvent event) {
        realtimeEventPublisher.publish(new RealtimeEvent(
                "sse",
                "notifications.created",
                List.of(event.notification().receiverId()),
                event.notification()
        ));
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void on(BinaryContentUpdatedEvent event) {
        realtimeEventPublisher.publish(new RealtimeEvent(
                "sse",
                "binaryContents.updated",
                List.of(),
                event.binaryContent()
        ));
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void on(ChannelChangedEvent event) {
        realtimeEventPublisher.publish(new RealtimeEvent(
                "sse",
                event.eventName(),
                List.of(),
                event.channel()
        ));
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void on(UserChangedEvent event) {
        realtimeEventPublisher.publish(new RealtimeEvent(
                "sse",
                event.eventName(),
                List.of(),
                event.user()
        ));
    }
}
