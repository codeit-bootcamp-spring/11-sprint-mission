package com.sprint.mission.discodeit.event;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

// @Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationCreator notificationCreator;

    @Async("eventTaskExecutor")
    @TransactionalEventListener
    public void on(MessageCreatedEvent event) {
        notificationCreator.create(event);
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener
    public void on(RoleUpdatedEvent event) {
        notificationCreator.create(event);
    }
}
