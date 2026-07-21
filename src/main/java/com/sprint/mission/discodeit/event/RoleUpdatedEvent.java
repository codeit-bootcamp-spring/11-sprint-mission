package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Role;
import java.util.UUID;

/**
 * 사용자의 권한(Role)이 변경되었음을 의미하는 이벤트입니다.
 * 알림 생성 리스너가 이 이벤트를 받아 권한이 변경된 당사자에게 알림을 생성합니다.
 */
public record RoleUpdatedEvent(
    UUID userId,
    Role previousRole,
    Role newRole
) {

}
