package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class UserStatus extends BaseEntity {

    private UUID userId;
    private Instant lastOnlineTime; // 시간 단위 계산을 위해서 (찾아보니깐 시간 단위로 측정을 하는 건 반드시 필요)
    private UserStatusType userStatusType; // 유저가 수동으로 설정할 수 있는 상태

    private UserStatus(UUID userId, Instant lastOnlineTime, UserStatusType userStatusType) {
        super();
        this.userId = userId;
        this.lastOnlineTime = lastOnlineTime;
        this.userStatusType = userStatusType;
    }

    private UserStatus(UserStatus other) {
        super(other);
        this.userId = other.userId;
        this.lastOnlineTime = other.lastOnlineTime;
        this.userStatusType = other.userStatusType;
    }

    @Override
    public UserStatus copy() {
        return new UserStatus(this);
    }

    public static UserStatus create(UUID userId) {
        return new UserStatus(userId, Instant.now(), UserStatusType.OFFLINE);
    }

    // 이하 로직
    public void updateLastOnlineTime() {
        this.lastOnlineTime = Instant.now();
        touch();
    }

    public void updateUserStatusType(UserStatusType userStatusType) {
        this.userStatusType = userStatusType;
        // 유저가 앱을 켜서 상태 정보를 수정했다는 의미로 마지막 접속 시간을 현재 시간으로 명시
        this.lastOnlineTime = Instant.now();
        touch();
    }

    public String calculateCurrentStatus() {
        // 디스코드를 보면 유저가 강제로 OFFLINE으로 설정할 수 있음
        if (this.userStatusType == UserStatusType.OFFLINE) {
            return UserStatusType.OFFLINE.name();
        }

        // 5분 동안 아무 통신이 없었다면
        Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);
        if (this.lastOnlineTime == null || this.lastOnlineTime.isBefore(fiveMinutesAgo)) {
            return UserStatusType.OFFLINE.name();
        }

        return this.userStatusType.name();
    }
}