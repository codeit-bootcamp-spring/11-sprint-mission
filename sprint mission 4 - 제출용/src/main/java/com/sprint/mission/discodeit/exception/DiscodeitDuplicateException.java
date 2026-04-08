package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class DiscodeitDuplicateException extends DiscodeitException {

  public DiscodeitDuplicateException(String message) {
    super(message);
  }

  public static DiscodeitDuplicateException user(String userName) {
    return new DiscodeitDuplicateException("이미 존재하는 유저입니다. userName=" + userName);
  }

  public static DiscodeitDuplicateException email(String email) {
    return new DiscodeitDuplicateException("이미 존재하는 email입니다. email=" + email);
  }

  public static DiscodeitDuplicateException channel(String channelName) {
    return new DiscodeitDuplicateException("이미 존재하는 채널명입니다. channelName=" + channelName);
  }

  public static DiscodeitDuplicateException readStatus(UUID userId, UUID channelId) {
    return new DiscodeitDuplicateException(
        "이미 존재하는 ReadStatus입니다. userId=" + userId + " channelId=" + channelId);
  }

  public static DiscodeitDuplicateException userStatus(UUID userId) {
    return new DiscodeitDuplicateException("이미 존재하는 UserStatus입니다. userId=" + userId);
  }
}
