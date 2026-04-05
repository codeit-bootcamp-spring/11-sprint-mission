package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageDto {

    public record CreateRequest(
            @NotNull(message = "작성자 ID는 필수 항목입니다.")
            UUID authorId,

            @NotNull(message = "채널 ID는 필수 항목입니다.")
            UUID channelId,

            @NotBlank(message = "메시지 내용을 입력해주세요.")
            @Size(max = 2000, message = "메시지는 2000자를 초과할 수 없습니다.")
            String content
    ) {
        // DTO -> Entity
        public Message toEntity(List<UUID> attachmentIds) {
            return Message.builder()
                    .authorId(this.authorId)
                    .channelId(this.channelId)
                    .content(this.content)
                    .attachmentIds(attachmentIds != null ? new ArrayList<>(attachmentIds) : new ArrayList<>())
                    .build();
        }
    }

    public record UpdateRequest(
            @NotBlank(message = "수정할 메시지 내용을 입력해주세요.")
            @Size(max = 2000, message = "메시지는 2000자를 초과할 수 없습니다.")
            String content
    ) {}

    public record Response(
        UUID id,
        String content,
        List<UUID> attachmentIds,
        Instant createdAt
    ) {
        // Entity -> DTO
        public static Response of(Message message) {
            return new Response(
                message.getId(),
                message.getContent(),
                message.getAttachmentIds(),
                message.getCreatedAt()
            );
        }
    }
}