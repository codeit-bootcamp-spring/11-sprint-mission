package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

public class ChannelDto {

  // PUBLIC 채널 생성
  public record CreatePublicRequest(
      @NotBlank(message = "채널 이름은 필수 항목입니다.")
      @Size(max = 100, message = "채널 이름은 100자를 초과할 수 없습니다.")
      String name,

      @Size(max = 500, message = "채널 설명은 500자를 초과할 수 없습니다.")
      String description
  ) {

    // DTO -> Entity
    // 엔티티의 정적 팩토리 메서드 호출
    public Channel toEntity() {
      return Channel.createPublic(this.name, this.description);
    }

  }

  // PRIVATE 채널 생성
  public record CreatePrivateRequest(
      List<UUID> participantIds
  ) {

    // DTO -> Entity
    // 엔티티의 정적 팩토리 메서드 호출
    public Channel toEntity() {
      return Channel.createPrivate();
    }
  }

  public record UpdateRequest(
      @Size(max = 100, message = "채널 이름은 100자를 초과할 수 없습니다.")
      String newName,

      @Size(max = 500, message = "채널 설명은 500자를 초과할 수 없습니다.")
      String newDescription
  ) {

  }

  @Builder
  public record Response(
      UUID id,
      ChannelType type,
      String name,
      String description,
      List<UserDto.Response> participants,
      Instant lastMessageAt
  ) {

  }
}