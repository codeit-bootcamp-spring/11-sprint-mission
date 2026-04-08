package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

public class MessageDto {

  public record CreateRequest(

      @NotBlank(message = "메시지 내용을 입력해주세요.")
      @Size(max = 2000, message = "메시지는 2000자를 초과할 수 없습니다.")
      String content,

      @NotNull(message = "채널 ID는 필수 항목입니다.")
      UUID channelId,

      @NotNull(message = "작성자 ID는 필수 항목입니다.")
      UUID authorId

  ) {

    // DTO -> Entity
    public Message toEntity(List<UUID> attachmentIds) {
      return Message.builder()
          .content(this.content)
          .channelId(this.channelId)
          .authorId(this.authorId)
          .attachmentIds(attachmentIds != null ? new ArrayList<>(attachmentIds) : new ArrayList<>())
          .build();
    }
  }

  public record UpdateRequest(
      @NotBlank(message = "수정할 메시지 내용을 입력해주세요.")
      @Size(max = 2000, message = "메시지는 2000자를 초과할 수 없습니다.")
      String newContent
  ) {

  }

  @Builder
  public record Response(
      UUID id,
      Instant createdAt,
      Instant updatedAt,
      String content,
      UUID channelId,
      UUID authorId,
      List<UUID> attachmentIds
  ) {

    // Entity -> DTO
    public static Response of(Message message) {
      return Response.builder()
          .id(message.getId())
          .createdAt(message.getCreatedAt())
          .updatedAt(message.getUpdatedAt())
          .content(message.getContent())
          .channelId(message.getChannelId())
          .authorId(message.getAuthorId())
          .attachmentIds(message.getAttachmentIds())
          .build();
    }
  }
}