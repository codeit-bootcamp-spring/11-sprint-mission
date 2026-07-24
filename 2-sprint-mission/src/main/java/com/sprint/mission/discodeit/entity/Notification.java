package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

  @Column(columnDefinition = "uuid", nullable = false)
  private UUID receiverId;

  @Column(length = 255, nullable = false)
  private String title;

  @Column(columnDefinition = "text", nullable = false)
  private String content;

  @Builder
  public Notification(UUID receiverId, String title, String content) {
    this.receiverId = receiverId;
    this.title = title;
    this.content = content;
  }
}