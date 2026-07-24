package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.service.MessageService;
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
  private final MessageService messageService;

  private static final String TOPIC = "discodeit.MessageCreatedEvent.v2";

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publishToKafka(MessageCreatedEvent event) {
    kafkaTemplate.send(TOPIC, event);
  }

  @KafkaListener(topics = TOPIC, groupId = "discodeit-group-v7")
  public void consumeFromKafkaAndBroadcast(MessageCreatedEvent event) {
    String destination = "/sub/channels." + event.getChannelId() + ".messages";

    MessageDto messageDto = messageService.findById(event.getMessageId());
    messagingTemplate.convertAndSend(destination, messageDto);

    log.info("Kafka 이벤트 수신 후 웹소켓 로컬 브로드캐스팅 완료 - 목적지: {}", destination);
  }
}