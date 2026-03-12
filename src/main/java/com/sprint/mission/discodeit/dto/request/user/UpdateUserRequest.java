package com.sprint.mission.discodeit.dto.request.user;

import com.sprint.mission.discodeit.dto.request.binaryContent.CreateBinaryContentRequest;
import lombok.Getter;

@Getter
public class UpdateUserRequest {
    private String username;
    private String email;
    private String password;
    private CreateBinaryContentRequest profile;
}
