package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class DiscodeitException extends RuntimeException {
    public DiscodeitException(String message) {
        super(message);
    }

    // notfound
    public static DiscodeitException userNotFound(UUID id) {
        return new DiscodeitException("존재하지 않는 유저입니다. id=" + id);
    }

    public static DiscodeitException channelNotFound(UUID id) {
        return new DiscodeitException("존재하지 않는 채널입니다. id=" + id);
    }

    public static DiscodeitException messageNotFound(UUID id) {
        return new DiscodeitException("존재하지 않는 메세지입니다. id=" + id);
    }

    public static DiscodeitException readStatusNotFound(UUID userId, UUID channelId) {
        return new DiscodeitException("존재하지 않는 ReadStatus입니다. userId=" + userId + " channelId=" + channelId);
    }

    public static DiscodeitException userStatusNotFound(UUID userId) {
        return new DiscodeitException("존재하지 않는 UserStatus입니다. userId=" + userId);
    }

    public static DiscodeitException binaryContentNotFound(UUID id) {
        return new DiscodeitException("존재하지 않는 파일입니다. id=" + id);
    }

    // 중복 체크
    public static DiscodeitException duplicateUser(String userName) {
        return new DiscodeitException("이미 존재하는 유저입니다. userName=" + userName);
    }

    public static DiscodeitException duplicateEmail(String email) {
        return new DiscodeitException("이미 존재하는 email입니다. email=" + email);
    }

    public static DiscodeitException duplicateChannel(String channelName) {
        return new DiscodeitException("이미 존재하는 채널명입니다. channelName=" + channelName);
    }

    public static DiscodeitException duplicateReadStatus(UUID userId, UUID channelId) {
        return new DiscodeitException("이미 존재하는 ReadStatus입니다. userId=" + userId + " channelId=" + channelId);
    }

    public static DiscodeitException duplicateUserStatus(UUID userId) {
        return new DiscodeitException("이미 존재하는 UserStatus입니다. userId=" + userId);
    }

    // blank, null
    public static DiscodeitException blankField(String fieldName) {
        return new DiscodeitException(fieldName + "은(는) null이거나 blank입니다.");
    }

}
