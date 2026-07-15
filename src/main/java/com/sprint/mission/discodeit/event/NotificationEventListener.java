package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    @Async
    @TransactionalEventListener
    public void on(MessageCreatedEvent event) {
        Message message = messageRepository.findById(event.messageId()).orElseThrow();

        List<ReadStatus> readStatuses =
                readStatusRepository.findAllByChannel_IdAndNotificationEnabledTrue(
                        message.getChannel().getId()
                );

        for (ReadStatus readStatus : readStatuses) {
            User receiver = readStatus.getUser();

            if (receiver.getId().equals(message.getAuthor().getId())) {
                continue;
            }

            notificationRepository.save(new Notification(
                    receiver,
                    message.getAuthor().getUsername() + " (#" + message.getChannel().getName() + ")",
                    message.getContent()
            ));
            evictNotificationCache(receiver.getId());
        }
    }

    @Async
    @TransactionalEventListener
    public void on(RoleUpdatedEvent event) {
        User receiver = userRepository.findById(event.userId()).orElseThrow();

        notificationRepository.save(new Notification(
                receiver,
                "권한이 변경되었습니다.",
                event.previousRole() + " -> " + event.newRole()
        ));
        evictNotificationCache(receiver.getId());
    }

    private void evictNotificationCache(java.util.UUID receiverId) {
        Cache cache = cacheManager.getCache("notificationsByReceiver");
        if (cache != null) {
            cache.evict(receiverId);
        }
    }
}
