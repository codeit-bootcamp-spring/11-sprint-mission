package com.sprint.mission.discodeit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

  @Column(nullable = false, updatable = false)
  private UUID receiverId;

  @Column(nullable = false, updatable = false)
  private String title;

  @Column(nullable = false, updatable = false, columnDefinition = "text")
  private String content;

  public Notification(UUID receiverId, String title, String content) {
    this.receiverId = receiverId;
    this.title = title;
    this.content = content;
  }
}