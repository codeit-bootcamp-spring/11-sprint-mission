package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
public class User extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String name;
    private String email;
    private List<Message> sentMessages;
    private List<Message> receivedMessages;
    private List<Channel> joinedChannels;
    private List<Channel> ownedChannels;

    public User(String name, String email) {
        super();
        this.name = name;
        this.email = email;
        this.sentMessages = new ArrayList<>();
        this.receivedMessages = new ArrayList<>();
        this.joinedChannels = new ArrayList<>();
        this.ownedChannels = new ArrayList<>();
    }

    public void update(String name, String email) {
        this.name = name;
        this.email = email;
        setUpdatedAt(Instant.now());
    }
}
