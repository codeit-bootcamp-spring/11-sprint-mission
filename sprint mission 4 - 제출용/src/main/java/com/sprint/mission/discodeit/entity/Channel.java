package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class Channel extends BaseEntity {

  private String channelName;
  private String channelDescription;
  private ChannelType channelType;

  public Channel(String channelName, String channelDescription, ChannelType channelType) {
    super();
    this.channelName = channelName;
    this.channelDescription = channelDescription;
    this.channelType = channelType;
  }

  public void updateChannel(String channelName, String channelDescription) {
    this.channelName = channelName;
    this.channelDescription = channelDescription;
    updateTimestamp();
  }

  @Override
  public String toString() {
    return
        "Channel ---- " + "[id=" + id + "] " +
            "[channelName='" + channelName + "'] " +
            "[channelDescription='" + channelDescription + "']" +
            " [createdAt='" + createdAt + "']" +
            " [updatedAt='" + updatedAt + "']";
  }
}
