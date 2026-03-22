package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends BaseEntity {
    private String content;
    private User sender;
    private Channel channel;
    private List<UUID> attachments;

    public Message(MessageCreateRequest messageCreateRequest, User sender, Channel channel, List<BinaryContent> attachments) {
        this.content = messageCreateRequest.content();
        this.sender = sender;
        this.channel = channel;
        this.attachments = new ArrayList<>(attachments.stream().map(BinaryContent::getId).toList());
    }

    public void updateContent(String content) {
        this.content = content;
        this.setUpdatedAt();
    }

    public MessageResponse toResponse(List<BinaryContent> attachments) {
        return new MessageResponse(
                this.content,
                this.sender.getId(),
                this.channel.getId(),
                attachments.stream()
                        .map(BinaryContent::toResponse)
                        .toList()
        );
    }
}