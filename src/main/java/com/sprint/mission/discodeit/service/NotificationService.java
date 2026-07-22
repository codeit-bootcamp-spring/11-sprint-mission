package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.NotificationDto;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    List<NotificationDto> findAllByReceiverId(UUID receiverId);

    NotificationDto find(UUID notificationId);

    NotificationDto create(String title, String content, UUID receiverId);

    void delete(UUID notificationId);
}
