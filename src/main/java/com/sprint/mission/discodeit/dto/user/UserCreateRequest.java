package com.sprint.mission.discodeit.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequest {//유저 생성 시
    private String userName;
    private String email;
    private String password;
    private UUID profileId;
}
