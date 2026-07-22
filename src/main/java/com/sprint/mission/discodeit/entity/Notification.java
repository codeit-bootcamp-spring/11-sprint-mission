package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor
@Builder
public class Notification extends BaseEntity {

  @Column(name = "receiver_id", nullable = false)
  UUID receiveId;

  @Column(nullable = false, length = 255)
  String title;

  @Column(nullable = false, length = 512)
  String content;


  public Notification(UUID receiveId, String title, String content) {
    this.receiveId = receiveId;
    this.title = title;
    this.content = content;
  }

  public static NotificationDto toDto(Notification notification) {
    return new NotificationDto(
        notification.getId(),
        notification.getCreatedAt(),
        notification.getReceiveId(),
        notification.getTitle(),
        notification.getContent()
    );


  }


}
