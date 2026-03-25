package com.sprint.mission.discodeit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
public class User extends BaseEntity {
    private String userName;
    private String nickname;
    private String description;
    private String email;
    private String password;
    private UUID profileImageId;



    public void update(String newUserName, String newNickname, String newDescription, String newEmail, String newPassword) {
        boolean anyValueUpdated = false;

        if (newUserName != null && !newUserName.equals(this.userName)) {
            this.userName = newUserName;
            anyValueUpdated = true;
        }
        if (newNickname != null && !newNickname.equals(this.nickname)) {
            this.nickname = newNickname;
            anyValueUpdated = true;
        }
        if (newDescription != null && !newDescription.equals(this.description)) {
            this.description = newDescription;
            anyValueUpdated = true;
        }
        if (newEmail != null && !newEmail.equals(this.email)) {
            this.email = newEmail;
            anyValueUpdated = true;
        }
        if (newPassword != null && !newPassword.equals(this.password)) {
            this.password = newPassword;
            anyValueUpdated = true;
        }

        if (anyValueUpdated) {
            super.timeUpdate();
        }
    }

    // 프로필 이미지 수정
    public void updateProfileImage(UUID newProfileImageId) {
        if (newProfileImageId != null && !newProfileImageId.equals(this.profileImageId)) {
            this.profileImageId = newProfileImageId;
        }
        super.timeUpdate();
    }

    @Override
    public String toString() {
        return "사용자 [" +
                "UUID: " + getId() +
                "\n이름: " + getUserName() +
                ", 별명: " + getNickname() +
                ", 소개: " + getDescription() +
                ", 이메일: " + getEmail() +
                ", 비밀번호: " + getPassword() + // 추후 비밀번호 관련 로직 변경 예정
                ", 프로필 사진: " + getProfileImageId() +
                ", 생성 시간: " + getCreatedAt() +
                ", 수정 시간: " + getUpdatedAt() +
                "]\n" ;
    }

}
