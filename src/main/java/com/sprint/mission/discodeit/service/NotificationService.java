package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.notification.NotificationResponse;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

  NotificationResponse createNotification(UUID receiverId, String title, String content);

  List<NotificationResponse> findAllByReceiverId(UUID receiverId);

  void deleteNotification(UUID id, UUID receiverId);
}