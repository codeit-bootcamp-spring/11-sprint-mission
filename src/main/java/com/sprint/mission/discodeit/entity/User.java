package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;

import java.util.UUID;

@Getter
public class User extends BaseUpdatableEntity {

    private String username;
    private String email;
    private String password;
    private UUID profileId;

    public User(String username, String email, String password, UUID profileId) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    public void setUsername(String username) {
        this.username = username;
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
                "name='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
