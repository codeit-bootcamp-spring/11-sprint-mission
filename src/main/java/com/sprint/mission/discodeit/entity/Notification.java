package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor
public class Notification extends BaseEntity {

  // User 전체를 가져오지 않고 PK만 가지고 옴
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = false)
  private User receiver;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  private Notification(User receiver, String title, String content) {
    this.receiver = receiver;
    this.title = title;
    this.content = content;
  }

  public static Notification create(User receiver, String title, String content) {
    return new Notification(receiver, title, content);
  }

}
