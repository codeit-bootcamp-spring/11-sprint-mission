package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.authority.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationCreator {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void create(MessageCreatedEvent event) {
        List<User> receivers = userRepository.findAllById(event.receiverIds());
        List<Notification> notifications = receivers.stream()
                .map(receiver -> new Notification(
                        receiver,
                        event.message().author().username()
                                + " (#" + event.channelName() + ")",
                        event.message().content()
                ))
                .toList();

        List<Notification> savedNotifications =
                notificationRepository.saveAll(notifications);
        savedNotifications.forEach(this::publishCreatedEvent);
        receivers.stream()
                .map(User::getId)
                .forEach(this::evictNotificationCache);
    }

    @Transactional
    public void create(RoleUpdatedEvent event) {
        User receiver = userRepository.findById(event.userId()).orElseThrow();

        Notification notification = notificationRepository.save(new Notification(
                receiver,
                "권한이 변경되었습니다.",
                event.previousRole() + " -> " + event.newRole()
        ));
        publishCreatedEvent(notification);
        evictNotificationCache(receiver.getId());
    }

    @Transactional
    public void create(S3UploadFailedEvent event) {
        String content = "Task: " + event.taskName() + "\n"
                + "RequestId: " + event.requestId() + "\n"
                + "BinaryContentId: " + event.binaryContentId() + "\n"
                + "Error: " + event.errorMessage();

        List<User> admins = userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .toList();
        List<Notification> notifications = admins.stream()
                .map(admin -> new Notification(
                        admin,
                        "바이너리 데이터 저장 실패",
                        content
                ))
                .toList();

        List<Notification> savedNotifications =
                notificationRepository.saveAll(notifications);
        savedNotifications.forEach(this::publishCreatedEvent);
        admins.stream()
                .map(User::getId)
                .forEach(this::evictNotificationCache);
    }

    private void evictNotificationCache(UUID receiverId) {
        Cache cache = cacheManager.getCache("notificationsByReceiver");
        if (cache != null) {
            cache.evict(receiverId);
        }
    }

    private void publishCreatedEvent(Notification notification) {
        eventPublisher.publishEvent(new NotificationCreatedEvent(
                notificationMapper.toDto(notification)
        ));
    }
}
