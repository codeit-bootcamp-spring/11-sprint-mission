package com.sprint.mission.discodeit.support;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;

public final class SecurityTestUtils {

    private SecurityTestUtils() {
    }

    public static DiscodeitUserDetails userDetails(User user) {
        UserDto userDto = new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null,
                true,
                user.getRole()
        );

        return new DiscodeitUserDetails(userDto, user.getPassword());
    }
}
