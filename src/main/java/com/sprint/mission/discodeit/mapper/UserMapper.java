package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user, UserStatus userStatus) {
        BinaryContentDto profileDto = null;
        if (user.getProfile() != null) {
            profileDto = new BinaryContentDto(
                    user.getProfile().getId(),
                    user.getProfile().getCreatedAt(),
                    user.getProfile().getFileName(),
                    user.getProfile().getFileSize(),
                    user.getProfile().getContentType()
            );
        }

        Boolean online = userStatus != null ? userStatus.isOnline() : null;

        return new UserDto(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getEmail(),
                profileDto,
                online
        );
    }
}