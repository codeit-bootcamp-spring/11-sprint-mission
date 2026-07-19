package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    log.debug("notification message-created trial: channelId={}, authorId={}", event.channelId(),
        event.authorId());
    String title = "%s (#%s)".formatted(event.authorName(), event.channelName());

    this.readStatusRepository.findAllByChannelIdAndNotificationEnabledTrue(event.channelId())
        .stream()
        .map(ReadStatus::getUser)
        .map(User::getId)
        .filter(userId -> !userId.equals(event.authorId()))
        .forEach(receiverId ->
            this.notificationService.createNotification(receiverId, title, event.content()));

    log.info("notification message-created success: channelId={}", event.channelId());
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    log.debug("notification role-updated trial: userId={}, oldRole={}, newRole={}",
        event.userId(), event.oldRole(), event.newRole());
    String content = "%s -> %s".formatted(event.oldRole(), event.newRole());

    this.notificationService.createNotification(
        event.userId(), "권한이 변경되었습니다.", content);

    log.info("notification role-updated success: userId={}", event.userId());
  }

  @EventListener
  public void on(S3UploadFailedEvent event) {
    log.debug("notification s3-upload-failed trial: binaryContentId={}", event.binaryContentId());
    String content = "RequestId: %s\nBinaryContentId: %s\nError: %s".formatted(
        event.requestId(), event.binaryContentId(), event.errorMessage());

    this.userRepository.findAllByRole(Role.ADMIN)
        .forEach(admin -> this.notificationService.createNotification(
            admin.getId(), "S3 파일 업로드 실패", content));

    log.info("notification s3-upload-failed success: binaryContentId={}", event.binaryContentId());
  }
}