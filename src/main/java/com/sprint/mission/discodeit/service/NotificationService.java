package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

  NotificationDto create(UUID receiverId, String title, String content);

  // 알림 조회(Id만 조회하기 때문에 아직까지는 N+1이 생기지 않음)
  List<NotificationDto> findAll(UUID receiverId);

  // 알림 확인
  void delete(UUID notificationId, UUID userId);
}
