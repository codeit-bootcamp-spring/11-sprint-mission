package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "channel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Channel extends BaseUpdatableEntity {

  @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Message> messages = new ArrayList<>();

  @Column(nullable = false)
  private String name;

  private String description;

  @Enumerated(EnumType.STRING) // Enum 이름을 문자열로 저장
  @Column(nullable = false)
  private ChannelType type;

  @Builder
  public Channel(String name, String description, ChannelType type) {
    this.name = name;
    this.description = description;
    this.type = type;
  }

  public void update(String newName, String newDescription) {
    if (newName != null) {
      this.name = newName;
    }
    if (newDescription != null) {
      this.description = newDescription;
    }
  }
}