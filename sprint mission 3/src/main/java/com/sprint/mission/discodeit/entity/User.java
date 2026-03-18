package com.sprint.mission.discodeit.entity;
import lombok.Getter;
import java.io.Serializable;

@Getter
public class User extends BaseEntity implements Serializable {
    private String userName;
    private String userEmail;
    private String userPassword;
    private static final long serialVersionUID = 1L;

    public User(String userName, String userEmail, String userPassword) {
        super();
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public void updateUser(String userName, String userEmail, String userPassword) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        updateTimestamp();
    }

    public void validateService(){
        if (this.userName == null || this.userName.isBlank()) {
            throw new IllegalArgumentException("유저명이 null이거나 blank입니다.");
        }
        if (this.userEmail == null || this.userEmail.isBlank()) {
            throw new IllegalArgumentException("email이 null이거나 blank입니다.");
        }
        if (this.userPassword == null || this.userPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호가 null이거나 blank입니다.");
        }
    }

    @Override
    public String toString() {return "User ---- " +
            "[id=" + id +
            "] [userName='" + userName + '\'' +
            "] [userEmail='" + userEmail + '\'' +
            "] [userPassword='" + userPassword + '\'' +
            "] [createdAt=" + createdAt + '\'' +
            "] [updatedAt=" + updatedAt + '\'' +
            "]";}
}
