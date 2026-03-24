package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel extends BaseEntity {
    private String name;
    private String description;
    private boolean isPrivate;

    public Channel(PublicChannelCreateRequest publicChannelCreateRequest) {
        this.name = publicChannelCreateRequest.name();
        this.description = publicChannelCreateRequest.description();
        this.isPrivate = false;
    }

    public Channel() {
        this.isPrivate = true;
    }

    public void updateName(String name) {
        this.name = name;
        this.setUpdatedAt();
    }

    public void updateDescription(String description) {
        this.description = description;
        this.setUpdatedAt();
    }

    public ChannelResponse toResponse(List<Message> messages, List<UUID> participants) {
        return new ChannelResponse(
                this.name,
                this.description,
                this.isPrivate,
                messages.stream()
                        .map(Message::getCreatedAt)
                        .max(Comparator.naturalOrder())
                        .orElse(null),
                this.isPrivate ? participants : null
        );
    }
}
