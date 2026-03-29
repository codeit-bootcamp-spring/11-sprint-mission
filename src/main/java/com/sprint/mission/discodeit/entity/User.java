package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.UUID;

@Getter
public class User extends BaseEntity {
    private String nickname;
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private UUID profileId;

    public User(String nickname, String username, String email, String password, String phoneNumber, UUID profileId) {
        this.nickname = nickname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.profileId = profileId;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.setUpdatedAt();
    }

    public void updateUsername(String username) {
        this.username = username;
        this.setUpdatedAt();
    }

    public void updateEmail(String email) {
        this.email = email;
        this.setUpdatedAt();
    }

    public void updatePassword(String password) {
        this.password = password;
        this.setUpdatedAt();
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.setUpdatedAt();
    }

    public void updateProfileId(UUID profileId) {
        this.profileId = profileId;
        this.setUpdatedAt();
    }
}
