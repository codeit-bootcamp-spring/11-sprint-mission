package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MessageMapper {

    public MessageDto toDto(Message message) {
        List<UUID> attachmentIds = message.getAttachments().stream()
                .map(attachment -> attachment.getId())
                .toList();

        return new MessageDto(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getContent(),
                message.getChannel().getId(),
                message.getAuthor() != null ? message.getAuthor().getId() : null,
                attachmentIds
        );
    }
}