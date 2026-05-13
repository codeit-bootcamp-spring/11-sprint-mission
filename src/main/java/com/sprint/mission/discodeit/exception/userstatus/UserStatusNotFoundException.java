package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserStatusNotFoundException extends UserStatusException {

  // 생성자를 private로 막아서 외부에서 생성하지 못하고 내부의 정적 팩토리 메서드로만 생성할 수 있도록 함
  private UserStatusNotFoundException(Map<String, Object> details) {
    super(ErrorCode.USER_STATUS_NOT_FOUND, details);
  }

  // userId로 찾을 떄도 있고 userStatusId로 찾을 때도 있으므로 정적 팩토리 메서드를 사용
  public static UserStatusNotFoundException byUserId(UUID userId) {
    return new UserStatusNotFoundException(Map.of("userId", userId));
  }

  public static UserStatusNotFoundException byStatusId(UUID statusId) {
    return new UserStatusNotFoundException(Map.of("userStatusId", statusId));
  }
}
