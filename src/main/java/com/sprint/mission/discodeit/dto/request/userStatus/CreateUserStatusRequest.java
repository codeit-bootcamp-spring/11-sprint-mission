package com.sprint.mission.discodeit.dto.request.userStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateUserStatusRequest {
    private UUID userId;
}
