package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.MessageAttachment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, UUID> {

}
