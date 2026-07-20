package com.sprint.mission.discodeit.event.notification;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.binarycontent.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Profile("!kafka")
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

    List<UUID> receiverIds = this.readStatusRepository
        .findAllByChannelIdAndNotificationEnabledTrue(event.channelId())
        .stream()
        .map(ReadStatus::getUser)
        .map(User::getId)
        .filter(userId -> !userId.equals(event.authorId()))
        .toList();
    this.notificationService.createNotification(receiverIds, title, event.content());

    log.info("notification message-created success: channelId={}", event.channelId());
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    log.debug("notification role-updated trial: userId={}, oldRole={}, newRole={}",
        event.userId(), event.oldRole(), event.newRole());
    String content = "%s -> %s".formatted(event.oldRole(), event.newRole());

    this.notificationService.createNotification(
        List.of(event.userId()), "권한이 변경되었습니다.", content);

    log.info("notification role-updated success: userId={}", event.userId());
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    log.debug("notification s3-upload-failed trial: binaryContentId={}", event.binaryContentId());
    String content = "RequestId: %s\nBinaryContentId: %s\nError: %s".formatted(
        event.requestId(), event.binaryContentId(), event.errorMessage());

    List<UUID> adminIds = this.userRepository.findAllByRole(Role.ADMIN).stream()
        .map(User::getId)
        .toList();
    this.notificationService.createNotification(adminIds, "S3 파일 업로드 실패", content);

    log.info("notification s3-upload-failed success: binaryContentId={}", event.binaryContentId());
  }
}