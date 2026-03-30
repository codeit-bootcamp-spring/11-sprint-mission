package com.sprint.mission.discodeit.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserUpdateRequest {

  @NotNull
  private UUID id;

  @NotBlank
  private String userName;

  @NotBlank
  @Email
  private String userEmail;

  @NotBlank
  private String userPassword;

  private String fileName;
  private byte[] fileContent;
  private String contentType;
}
