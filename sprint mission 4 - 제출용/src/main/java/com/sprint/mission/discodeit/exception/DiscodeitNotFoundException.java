package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class DiscodeitNotFoundException extends DiscodeitException {

  public DiscodeitNotFoundException(String message) {
    super(message);
  }

  public static DiscodeitNotFoundException user(UUID id) {
    return new DiscodeitNotFoundException("존재하지 않는 유저입니다. id=" + id);
  }

  public static DiscodeitNotFoundException channel(UUID id) {
    return new DiscodeitNotFoundException("존재하지 않는 채널입니다. id=" + id);
  }

  public static DiscodeitNotFoundException message(UUID id) {
    return new DiscodeitNotFoundException("존재하지 않는 메세지입니다. id=" + id);
  }

  public static DiscodeitNotFoundException readStatus(UUID id) {
    return new DiscodeitNotFoundException(
        "존재하지 않는 ReadStatus입니다.");
  }

  public static DiscodeitNotFoundException userStatus(UUID userId) {
    return new DiscodeitNotFoundException("존재하지 않는 UserStatus입니다. userId=" + userId);
  }

  public static DiscodeitNotFoundException binaryContent(UUID id) {
    return new DiscodeitNotFoundException("존재하지 않는 파일입니다. id=" + id);
  }
}
