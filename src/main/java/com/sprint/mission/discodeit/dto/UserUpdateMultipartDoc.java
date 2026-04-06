package com.sprint.mission.discodeit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserUpdateMultipartDoc {
    public UserUpdatePart userUpdateRequest;

    @Schema(type = "string", format = "binary")
    public String profile;
}