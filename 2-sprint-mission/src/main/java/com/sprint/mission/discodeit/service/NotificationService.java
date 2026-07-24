package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

  List<NotificationDto.Response> findAllByReceiverId(UUID receiverId);

  void delete(UUID id, UUID requesterId);

  void createForMessage(MessageCreatedEvent event);

  void createForRoleUpdate(RoleUpdatedEvent event);

  void createForS3UploadFailure(S3UploadFailedEvent event);
}