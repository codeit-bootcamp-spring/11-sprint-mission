package com.sprint.mission.discodeit.entity;

import lombok.Getter;

@Getter
public class Channel extends BaseEntity {

  private String name;
  private String description;
  private final boolean isPrivate;

  public Channel(String name, String description) {
    this.name = name;
    this.description = description;
    this.isPrivate = false;
  }

  public Channel() {
    this.isPrivate = true;
  }

  public void updateName(String name) {
    this.name = name;
    this.setUpdatedAt();
  }

  public void updateDescription(String description) {
    this.description = description;
    this.setUpdatedAt();
  }
}
