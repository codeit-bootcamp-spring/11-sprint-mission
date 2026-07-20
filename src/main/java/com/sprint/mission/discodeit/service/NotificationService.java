package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

  void createAll(MessageCreatedEvent event);

  void create(RoleUpdatedEvent event);

  NotificationDto find(UUID notificationId);

  List<NotificationDto> findAllByReceiverId(UUID receiverId);

  void delete(UUID notificationId);
}
