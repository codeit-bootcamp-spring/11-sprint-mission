package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class Channel extends BaseEntity {
    private String name;
    private String description;
    private boolean isPrivate;
    private List<User> participants;
    private List<Message> messages;

    public Channel(PublicChannelCreateRequest publicChannelCreateRequest) {
        this.name = publicChannelCreateRequest.name();
        this.description = publicChannelCreateRequest.description();
        this.isPrivate = false;
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public Channel(List<User> participants) {
        this.isPrivate = true;
        this.participants = new ArrayList<>(participants);
        this.messages = new ArrayList<>();
    }

    public void updateName(String name) {
        this.name = name;
        this.setUpdatedAt();
    }

    public void updateDescription(String description) {
        this.description = description;
        this.setUpdatedAt();
    }

    public void removeParticipant(User user) {
        this.participants.remove(user);
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public ChannelResponse toResponse() {
        return new ChannelResponse(
                this.name,
                this.description,
                this.isPrivate,
                this.messages.stream()
                        .map(Message::getCreatedAt)
                        .max(Comparator.naturalOrder())
                        .orElse(null),
                this.isPrivate
                        ? this.participants.stream()
                        .map(User::getId)
                        .toList()
                        : null
        );
    }
}
