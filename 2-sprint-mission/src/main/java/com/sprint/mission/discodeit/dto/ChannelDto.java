package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ChannelDto {

  // PUBLIC 채널 생성
  public record CreatePublicRequest(
      @NotBlank(message = "채널 이름은 필수 항목입니다.")
      @Size(max = 50, message = "채널 이름은 50자를 초과할 수 없습니다.")
      String name,

      @Size(max = 255, message = "채널 설명은 255자를 초과할 수 없습니다.")
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
      return Channel.createPrivate(this.participantIds);
    }
  }

  public record UpdateRequest(
      @Size(max = 50, message = "채널 이름은 50자를 초과할 수 없습니다.")
      String newName,

      @Size(max = 255, message = "채널 설명은 255자를 초과할 수 없습니다.")
      String newDescription
  ) {

  }

  @Builder
  public record Response(
      UUID id,
      ChannelType type,
      String name,
      String description,
      Instant lastMessageAt,
      List<UUID> participantIds
  ) {

    // Entity -> DTO
    public static Response of(Channel channel, Instant lastMessageAt, List<UUID> participantIds) {
      return Response.builder()
          .id(channel.getId())
          .type(channel.getType())
          .name(channel.getName())
          .description(channel.getDescription())
          .lastMessageAt(lastMessageAt)
          .participantIds(channel.getType() == ChannelType.PRIVATE ? participantIds : null)
          .build();
    }
  }
}