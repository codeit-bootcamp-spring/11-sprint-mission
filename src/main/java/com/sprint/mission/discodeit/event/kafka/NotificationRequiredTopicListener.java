package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.authority.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredTopicListener {

    private final ObjectMapper objectMapper;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    @KafkaListener(topics = "discodeit.MessageCreatedEvent")
    public void onMessageCreatedEvent(String kafkaEvent) {
        try {
            MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

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
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
    public void onRoleUpdatedEvent(String kafkaEvent) {
        try {
            RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
            User receiver = userRepository.findById(event.userId()).orElseThrow();

            notificationRepository.save(new Notification(
                    receiver,
                    "권한이 변경되었습니다.",
                    event.previousRole() + " -> " + event.newRole()
            ));
            evictNotificationCache(receiver.getId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
    public void onS3UploadFailedEvent(String kafkaEvent) {
        try {
            S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);
            String content = "Task: " + event.taskName() + "\n"
                    + "RequestId: " + event.requestId() + "\n"
                    + "BinaryContentId: " + event.binaryContentId() + "\n"
                    + "Error: " + event.errorMessage();

            userRepository.findAll().stream()
                    .filter(user -> user.getRole() == UserRole.ADMIN)
                    .forEach(admin -> {
                        notificationRepository.save(new Notification(
                                admin,
                                "바이너리 데이터 저장 실패",
                                content
                        ));
                        evictNotificationCache(admin.getId());
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void evictNotificationCache(UUID receiverId) {
        Cache cache = cacheManager.getCache("notificationsByReceiver");
        if (cache != null) {
            cache.evict(receiverId);
        }
    }
}
