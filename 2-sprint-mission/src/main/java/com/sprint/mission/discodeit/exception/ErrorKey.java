package com.sprint.mission.discodeit.exception;

import lombok.Getter;

@Getter
public enum ErrorKey {
  USER_ID("userId"),
  USERNAME("username"),
  EMAIL("email"),
  CHANNEL_ID("channelId"),
  READ_STATUS_ID("readStatusId"),
  MESSAGE_ID("messageId"),
  BINARY_CONTENT_ID("binaryContentId"),
  NOTIFICATION_ID("notificationId");

  private final String value;

  ErrorKey(String value) {
    this.value = value;
  }

}