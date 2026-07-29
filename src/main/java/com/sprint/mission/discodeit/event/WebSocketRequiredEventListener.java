package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.event.kafka.RealtimeEvent;
import com.sprint.mission.discodeit.event.kafka.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

    private final RealtimeEventPublisher realtimeEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessage(MessageCreatedEvent event) {
        realtimeEventPublisher.publish(new RealtimeEvent(
                "websocket",
                "/sub/channels." + event.message().channelId() + ".messages",
                List.of(),
                event.message()
        ));
    }
}
