package com.sprint.mission.discodeit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity(name = "channels")
public class Channel extends BaseUpdatableEntity {

  @Enumerated(EnumType.STRING)
  @Column(length = 10, nullable = false, updatable = false)
  private ChannelType type = ChannelType.PRIVATE;

  public enum ChannelType {
    PUBLIC,
    PRIVATE
  }

  @Column(length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  public Channel(String name, String description) {
    this.name = name;
    this.description = description;
    this.type = ChannelType.PUBLIC;
  }

  public void updateName(String name) {
    this.name = name;
  }

  public void updateDescription(String description) {
    this.description = description;
  }
}
