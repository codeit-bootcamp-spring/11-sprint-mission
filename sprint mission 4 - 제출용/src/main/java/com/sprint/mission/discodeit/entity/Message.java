package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class Message extends BaseEntity implements Serializable {

  private String messageContent;
  private UUID authorId;
  private UUID channelId;
  private UUID messageReceiver;
  private static final long serialVersionUID = 1L;

  public Message(String messageContent, UUID channelId, UUID authorId, UUID messageReceiver) {
    super();
    this.messageContent = messageContent;
    this.channelId = channelId;
    this.authorId = authorId;
    this.messageReceiver = messageReceiver;
  }

  public void updateContent(String messageContent) {
    this.messageContent = messageContent;
    updateTimestamp();
  }

  @Override
  public String toString() {
    return "Message ---- [" +
        "id = " + id +
        "] [channelId=" + channelId +
        "] [sender='" + authorId + '\'' +
        "], [receiver='" + messageReceiver + '\'' +
        "], [content='" + messageContent + "\']" +
        " [createdAt='" + createdAt + "']" +
        " [updatedAt='" + updatedAt + "']";

  }
}
