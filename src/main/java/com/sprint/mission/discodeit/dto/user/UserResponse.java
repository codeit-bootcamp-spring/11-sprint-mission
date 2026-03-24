package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;

public record UserResponse(
        String nickname,
        String username,
        String email,
        String phoneNumber,
        BinaryContentResponse profile,
        UserStatusResponse status
) {}
