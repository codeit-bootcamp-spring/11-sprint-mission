package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

  private final SimpMessagingTemplate messagingTemplate;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private static final String TOPIC = "discodeit.MessageCreatedEvent";

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publishToKafka(MessageCreatedEvent event) {
    log.debug("Kafka 토픽으로 메시지 브로드캐스팅 발행 준비 - channelId: {}, messageId: {}", event.getChannelId(),
        event.getMessageId());

    kafkaTemplate.send(TOPIC, event);
  }

  @KafkaListener(topics = TOPIC, groupId = "#{T(java.util.UUID).randomUUID().toString()}")
  public void consumeFromKafkaAndBroadcast(MessageCreatedEvent event) {
    String destination = "/sub/channels." + event.getChannelId() + ".messages";

    messagingTemplate.convertAndSend(destination, event);

    log.info("Kafka 이벤트 수신 후 웹소켓 로컬 브로드캐스팅 완료 - 목적지: {}", destination);
  }
}