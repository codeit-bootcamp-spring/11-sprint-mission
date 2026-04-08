package com.sprint.mission.discodeit.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

  @NotNull(message = "id는 필수입니다.")
  private UUID id;

  private String newUsername;

  @Email(message = "newEmail 형식이 올바르지 않습니다.")
  private String newEmail;

  private String newPassword;
}
