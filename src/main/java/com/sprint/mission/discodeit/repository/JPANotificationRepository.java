package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPANotificationRepository extends JpaRepository<Notification, UUID> {

  List<Notification> findAllByReceiveId(UUID receiveId);

}
