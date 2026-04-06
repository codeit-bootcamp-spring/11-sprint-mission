package com.sprint.mission.discodeit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserCreateMultipartDoc {
    public UserCreatePart userCreateRequest;

    @Schema(type = "string", format = "binary")
    public String profile;
}