package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.UUID;

@Getter
public class User extends BaseEntity {

    private String userName;
    private String statusMessage;
    private UUID profileId;
    private String email;
    private String password;

    public User(String userName, String email, String password, String statusMessage) {
        super();
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.statusMessage = statusMessage;
        this.profileId = null;
    }

    public void update(
            String userName,
            String email,
            String password,
            String statusMessage,
            UUID profileId
    ) {
        if (userName != null) this.userName = userName;
        if (email != null) this.email = email;
        if (password != null) this.password = password;
        if (statusMessage != null) this.statusMessage = statusMessage;
        if (profileId != null) this.profileId = profileId;

        touch();
    }

    @Override
    public String toString() {
        return "User{id=" + getId()
                + ", userName='" + userName + '\''
                + ", statusMessage='" + statusMessage + '\''
                + "}";
    }
}