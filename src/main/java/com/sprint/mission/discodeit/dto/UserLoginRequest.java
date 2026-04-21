package com.sprint.mission.discodeit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginRequest")
public record UserLoginRequest(String username, String password) {

}
