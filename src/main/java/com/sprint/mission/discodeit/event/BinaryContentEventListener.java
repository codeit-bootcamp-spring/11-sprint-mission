package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.entity.BinaryContent.BinaryContentStatus;
import lombok.RequiredArgsConstructor;
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
        }
    }
}
