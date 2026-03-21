package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Channel extends BaseEntity {
    private String name;
    private List<User> participants;
    private List<Message> messages;

    public Channel(String name) {
        this.name = name;
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public void updateName(String name) {
        this.name = name;
        this.setUpdatedAt();
    }

    public void removeParticipant(User user) {
        this.participants.remove(user);
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    @Override
    public String toString() {
        return "Channel{" +
                "name='" + this.name + '\'' +
                '}';
    }
}
