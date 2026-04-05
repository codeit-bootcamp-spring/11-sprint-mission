package com.sprint.mission.discodeit.dto.userdto;


import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


public record UpdateUserDto(
    String newUsername,
    String newPassword,
    String newEmail
) {

}
