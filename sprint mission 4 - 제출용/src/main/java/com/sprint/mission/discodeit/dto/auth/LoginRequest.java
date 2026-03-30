package com.sprint.mission.discodeit.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {

  @NotBlank
  private String userName;

  @NotBlank
  private String userPassword;
}
