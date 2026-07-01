package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "channel")
@Entity
@NoArgsConstructor
public class Channel extends BaseUpdatableEntity {

  @Column(length = 100)
  private String name; //채널 이름

  @Column(nullable = false, length = 10)
  @Enumerated(EnumType.STRING)
  private ChannelType type;

  @Column(length = 500)
  private String description;


  public Channel(String name, String description, ChannelType type) {
    this.name = name;
    this.description = description;
    this.type = type;
  }

  public void updateName(String name) {
    this.name = name;
  }

  public void updateDescription(String description) {
    this.description = description;
  }

  public enum ChannelType {
    PUBLIC,
    PRIVATE
  }


}
