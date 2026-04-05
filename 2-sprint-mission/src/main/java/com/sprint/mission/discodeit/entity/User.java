package com.sprint.mission.discodeit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
public class User extends BaseEntity {
    private String username;
    private String nickname;
    private String description;
    private String email;
    private String password;
    private UUID profileImageId;



    public void update(String newUsername, String newNickname, String newDescription, String newEmail, String newPassword) {
        boolean anyValueUpdated = false;

        if (newUsername != null && !newUsername.equals(this.username)) {
            this.username = newUsername;
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

    // 비밀번호 검증
    // 비밀번호 암호화는 Spring Security의 PasswordEncoder로 스프린트 미션에 맞추어 이후 진행 예정
    public void validatePassword(String password) {
        if (this.password == null || !this.password.equals(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    @Override
    public String toString() {
        return "사용자 [" +
                "UUID: " + getId() +
                "\n이름: " + getUsername() +
                ", 별명: " + getNickname() +
                ", 소개: " + getDescription() +
                ", 이메일: " + getEmail() +
 //               ", 비밀번호: " + getPassword() + // 추후 비밀번호 관련 로직 변경 예정 + 제외
                ", 프로필 사진: " + getProfileImageId() +
                ", 생성 시간: " + getCreatedAt() +
                ", 수정 시간: " + getUpdatedAt() +
                "]\n" ;
    }

}
