package com.sprint.mission.discodeit.dto.user;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
// User 조회 시 반환 데이터
public class UserResponse {
    private UUID id;
    private String userName;
    private String userEmail;
    private boolean isOnline;
}