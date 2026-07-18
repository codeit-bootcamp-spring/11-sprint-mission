package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
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
