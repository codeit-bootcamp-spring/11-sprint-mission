package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public class UserDto {

  @Builder
  public record CreateRequest(
      @NotBlank(message = "이름은 필수 항목입니다.")
      @Size(min = 2, max = 20, message = "이름은 2~20자 사이여야 합니다.")
      String username,

      @NotBlank(message = "이메일은 필수 항목입니다.")
      @Email(message = "올바른 이메일 형식이 아닙니다.")
      String email,

      @NotBlank(message = "비밀번호는 필수 항목입니다.")
      @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
          message = "비밀번호는 8자 이상으로 영문, 숫자, 특수문자를 포함해야 합니다.")
      String password
  ) {

    // DTO -> Entity
    public User toEntity(UUID profileImageId) {
      return User.builder()
          .username(this.username)
          .email(this.email)
          .password(this.password)
          .profileImageId(profileImageId)
          .build();
    }
  }

  public record UpdateRequest(
      @Size(min = 2, max = 10, message = "이름은 2~10자 사이여야 합니다.")
      String newUsername,

      @Email(message = "올바른 이메일 형식이 아닙니다.")
      String newEmail,

      @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
          message = "비밀번호는 8자 이상으로 영문, 숫자, 특수문자를 포함해야 합니다.")
      String newPassword
  ) {

  }

  @Builder
  public record Response(
      UUID id,
      Instant createdAt,
      Instant updatedAt,
      String username,
      String email,
      UUID profileId,
      Boolean online
  ) {

    // Entity -> DTO
    public static Response of(User user, UserStatus status) {
      return Response.builder()
          .id(user.getId())
          .createdAt(user.getCreatedAt())
          .updatedAt(user.getUpdatedAt())
          .username(user.getUsername())
          .email(user.getEmail())
          .profileId(user.getProfileImageId())
          .online(status.isOnline())
          .build();
    }
  }
}