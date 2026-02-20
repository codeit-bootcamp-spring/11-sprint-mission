package com.sprint.mission.discodeit.entity;

import java.util.ArrayList;
import java.util.List;

public class Channel extends Common{
    private String name;
    private List<User> participants;

    public Channel(String name, List<User> participants) {
        super();
        this.name = name;
        this.participants = new ArrayList<>(participants);
        // 인자 추가 시 수정
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getParticipants() {
        return participants;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "name='" + name + '\'' +
                ", participants=" + participants.stream()
                        .map(User::getName)
                        .toList() +
                '}';
    }
}
