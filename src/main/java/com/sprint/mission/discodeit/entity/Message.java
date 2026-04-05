package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class Message extends Entity {

  private String message;
  private final UUID senderId; //전송자 아이디
  private final UUID channelId; //채널 아이디
  private List<UUID> attachmentIds;


  public Message(UUID senderId, UUID channelId, String message, List<UUID> attachmentIds) {

    super();
    this.senderId = senderId;
    this.channelId = channelId;
    this.message = message;
    this.attachmentIds = attachmentIds;

  }


  public void updateMessage(String message) {
    this.message = message;
    super.updateUpdatedAt();
  }

  public void updateAttachmentIds(List<UUID> attachmentIds) {
    this.attachmentIds = attachmentIds;
    super.updateUpdatedAt();

  }

  @Override

  public String toString() {
    return "Message{" +
        "message='" + message + '\'' +
        ", authorId='" + senderId + '\'' +
        ", id='" + channelId + '\'' +
        '}';
  }
}
