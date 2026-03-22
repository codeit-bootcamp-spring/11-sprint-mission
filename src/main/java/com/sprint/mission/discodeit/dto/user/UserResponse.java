package com.sprint.mission.discodeit.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserResponse {//유저 조회용 DTO (패스워드 제외)
    private UUID id;
    private String userName;
    private String email;
    private UUID profileId;
    private boolean isOnline;
    private List<UUID> joinedChannelId;
}