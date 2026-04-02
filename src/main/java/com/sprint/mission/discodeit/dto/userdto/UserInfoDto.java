package com.sprint.mission.discodeit.dto.userdto;

import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record UserInfoDto(UUID userId,
                          String nickname,
                          String email,
                          UUID profileId,
                          UserStatus status


) {
}
