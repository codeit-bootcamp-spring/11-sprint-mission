package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * Kafka 도입(2.6) 이후 비활성화됨.
 * S3 업로드 실패 알림 책임은 {@code event.kafka.KafkaProduceRequiredEventListener}(발행)와
 * {@code event.kafka.NotificationRequiredTopicListener}(구독/알림 생성)로 이전되었다.
 */
@RequiredArgsConstructor
public class S3UploadFailureListener {

  private final NotificationService notificationService;
  private final UserRepository userRepository;

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    String content = """
        RequestId: %s
        BinaryContentId: %s
        Error: %s
        """.formatted(event.requestId(), event.binaryContentId(), event.errorMessage());

    String title = "파일 업로드 실패: " + event.taskName();

    for (User admin : userRepository.findAllByRole(Role.ADMIN)) {
      notificationService.create(admin.getId(), title, content);
    }
  }
}
