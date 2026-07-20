package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface NotificationService {

  /**
   * 여러 수신자에게 동일한 알림을 한 번에 생성합니다(배치 저장 + 캐시 무효화).
   * receiverIds가 비어있으면 아무 것도 하지 않고 빈 리스트를 반환합니다.
   */
  List<NotificationDto> create(Set<UUID> receiverIds, String title, String content);

  NotificationDto find(UUID notificationId);

  List<NotificationDto> findAllByReceiverId(UUID receiverId);

  void delete(UUID notificationId);
}
