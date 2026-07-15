package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.authority.UserRole;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.entity.BinaryContent.BinaryContentStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BinaryContentEventListener {

    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentRepository binaryContentRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(BinaryContentCreatedEvent event) {
        BinaryContent binaryContent = binaryContentRepository.findById(event.id())
                .orElseThrow();

        try {
            binaryContentStorage.put(event.id(), event.bytes());
            binaryContent.updateStatus(BinaryContentStatus.SUCCESS);
        } catch (Exception e) {
            binaryContent.updateStatus(BinaryContentStatus.FAIL);

            String requestId = MDC.get("requestId");
            String content = "Task: BinaryContentUpload\n"
                    + "RequestId: " + requestId + "\n"
                    + "BinaryContentId: " + event.id() + "\n"
                    + "Error: " + e.getMessage();

            userRepository.findAll().stream()
                    .filter(user -> user.getRole() == UserRole.ADMIN)
                    .forEach(admin -> notificationRepository.save(new Notification(
                            admin,
                            "바이너리 데이터 저장 실패",
                            content
                    )));
        }
    }
}
