package com.sprint.mission.discodeit.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    private String userName;
    private String email;
    private String password;
    private UUID profileId;// 선택사항, null이면 기존 이미지 유지
}