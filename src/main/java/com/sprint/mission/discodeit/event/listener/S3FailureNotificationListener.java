package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.dto.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.Role;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!kafka")
@RequiredArgsConstructor
public class S3FailureNotificationListener {

  private final NotificationService notificationService;
  private final UserRepository userRepository;

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    log.info("S3 업로드 실패 이벤트 수신. 관리자 알림 발송 시작");

    List<User> admins = userRepository.findAllByRole(Role.ADMIN);

    if (admins.isEmpty()) {
      log.error("알림을 받을 관리자(admin) 계정이 DB에 존재하지 않습니다.");
      return;
    }

    String title = "S3 파일 업로드 실패";
    String content = String.format("RequestId: %s\nBinaryContentId: %s\nError: %s",
        event.requestId(), event.binaryContentId(), event.errorMessage());

    admins.forEach(admin -> notificationService.createNotification(admin.getId(), title, content));

    log.info("관리자 알림 생성 완료 - 발송 건수: {}", admins.size());
  }
}
