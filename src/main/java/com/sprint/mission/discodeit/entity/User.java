package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class User extends Common{

    private String name;
    private String email;
    private String password;
    private UUID profileId;

    public User(String name, String email, String password, UUID profileId) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProfileId(UUID profileId) {
        this.profileId = profileId;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
