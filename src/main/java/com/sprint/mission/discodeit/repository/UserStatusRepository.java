package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Domain.UserStatus;

import java.time.Instant;
import java.util.UUID;

public interface UserStatusRepository {
    // 사용자 별 마지막으로 확인된 접속 시간을 표현하는 도메인 모델입니다. 사용자의 온라인 상태를 확인하기 위해 활용합니다.

    // 생성 > userId 기반으로 lastOnlineAt을 만드는거임.
    UserStatus create(UserStatus userStatus);

    // userid기반으로 조회하기
    UserStatus readByUserId(UUID userId);

    // update도 userid기반으로 함.
    void update(UUID userId, Instant lastOnlineAt);

    // 유저가 삭제되야 on/off도 사라지는거임.
    UserStatus delete(UUID userId);
}
