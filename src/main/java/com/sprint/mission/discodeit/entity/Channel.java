package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Channel extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String name;
    private int capacity;
    private User owner;
    private List<User> participants;
    private List<Message> messages;

    public Channel(String name, int capacity, User owner) {
        super();
        this.name = name;
        this.capacity = capacity;
        this.owner = owner;
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.participants.add(owner);
    }

    public boolean addParticipant(User user) {
        if (participants.size() >= capacity) {
            return false;
        }
        if (!participants.contains(user)) {
            participants.add(user);
            return true;
        }
        return false;
    }

    public boolean removeParticipant(User user) {
        return participants.remove(user);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void update(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        setUpdatedAt(Instant.now());
    }
}
