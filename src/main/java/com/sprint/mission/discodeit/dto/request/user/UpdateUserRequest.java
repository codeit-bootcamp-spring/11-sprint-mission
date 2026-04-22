package com.sprint.mission.discodeit.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateUserRequest {
    private String username;
    private String email;
    private String password;
}
