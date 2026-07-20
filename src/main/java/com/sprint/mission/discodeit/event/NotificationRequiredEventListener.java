package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredEventListener {

  private final NotificationService notificationService;

  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    notificationService.createAll(event);
  }

  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    notificationService.create(event);
  }
}
