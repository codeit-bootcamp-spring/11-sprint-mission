package com.sprint.mission.discodeit.dto.userdto;


import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


public record UpdateUserDto(
        UUID userId,
        String newNickname,
        String newEmail,
        String oldPassword,
        String newPassword,
        MultipartFile newProfileImg


        ) {
}
