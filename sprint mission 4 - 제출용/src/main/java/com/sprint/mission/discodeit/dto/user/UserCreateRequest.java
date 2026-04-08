package com.sprint.mission.discodeit.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

  @NotBlank(message = "username은 필수입니다.")
  private String username;

  @NotBlank(message = "email은 필수입니다.")
  @Email(message = "email 형식이 올바르지 않습니다.")
  private String email;

  @NotBlank(message = "password는 필수입니다.")
  private String password;
}
