package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class Message extends BaseEntity implements Serializable {

  private String content;
  private UUID authorId;
  private UUID channelId;
  private UUID messageReceiver;
  private static final long serialVersionUID = 1L;

  public Message(String content, UUID channelId, UUID authorId, UUID messageReceiver) {
    super();
    this.content = content;
    this.channelId = channelId;
    this.authorId = authorId;
    this.messageReceiver = messageReceiver;
  }

  public void updateContent(String content) {
    this.content = content;
    updateTimestamp();
  }

  @Override
  public String toString() {
    return "Message ---- [" +
        "id = " + id +
        "] [channelId=" + channelId +
        "] [sender='" + authorId + '\'' +
        "], [receiver='" + messageReceiver + '\'' +
        "], [content='" + content + "\']" +
        " [createdAt='" + createdAt + "']" +
        " [updatedAt='" + updatedAt + "']";

  }
}
