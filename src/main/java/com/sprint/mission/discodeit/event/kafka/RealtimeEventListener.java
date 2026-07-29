package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeEventListener {

    private final ObjectMapper objectMapper;
    private final SseService sseService;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "discodeit.RealtimeEvent",
            groupId = "${discodeit.realtime.group-id}"
    )
    public void on(String payload) {
        try {
            RealtimeEvent event =
                    objectMapper.readValue(payload, RealtimeEvent.class);

            if ("websocket".equals(event.type())) {
                messagingTemplate.convertAndSend(
                        event.eventName(),
                        event.data()
                );
                return;
            }

            if (event.receiverIds().isEmpty()) {
                sseService.broadcast(event.eventName(), event.data());
            } else {
                sseService.send(
                        event.receiverIds(),
                        event.eventName(),
                        event.data()
                );
            }
        } catch (JsonProcessingException e) {
            log.error("실시간 이벤트 역직렬화 실패: payload={}", payload, e);
        }
    }
}
