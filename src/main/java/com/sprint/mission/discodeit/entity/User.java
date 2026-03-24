package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
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

    public User(UserCreateRequest userCreateRequest, BinaryContent profile) {
        this.nickname = userCreateRequest.nickname();
        this.username = userCreateRequest.username();
        this.email = userCreateRequest.email();
        this.password = userCreateRequest.password();
        this.phoneNumber = userCreateRequest.phoneNumber();
        this.profileId = profile != null ? profile.getId() : null;
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

    public void updateProfile(BinaryContent profile) {
        this.profileId = profile.getId();
        this.setUpdatedAt();
    }

    public UserResponse toResponse(BinaryContent profile, UserStatus status) {
        return new UserResponse(
                this.nickname,
                this.username,
                this.email,
                this.phoneNumber,
                profile != null ? profile.toResponse() : null,
                status.toResponse()
        );
    }
}
