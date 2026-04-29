package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "channels")
@NoArgsConstructor
public class Channel extends BaseUpdatableEntity {

  @Column
  private String name = "";

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 10)
  private ChannelType type;

  @Column
  private String description = "";

  @Column(name = "last_message_at")
  private Instant lastMessageAt;

  @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Message> messages = new ArrayList<>();

  @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReadStatus> readStatuses = new ArrayList<>();

  public Channel(String name, String description, ChannelType type) {
    this.name = name;
    this.type = type;
    this.description = description;
  }

  // 필드를 수정하는 update 함수
  public void update(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public void updateLastMessageAt(Instant createdAt) {
    this.lastMessageAt = createdAt;
  }

  @Override
  public String toString() {
    return "채널명 : " + name + ", 채널 타입 : " + type;
  }
}
