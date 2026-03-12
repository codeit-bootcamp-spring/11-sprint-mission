package com.sprint.mission.discodeit.entity.Domain;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class User extends BaseEntity implements Serializable {
    private String userName;
    private String userNickname;
    private static final long serialVersionUID = 1L;

    public User(String userName, String userNickname) {
        super();
        this.userName = userName;
        this.userNickname = userNickname;
    }

    public void updateUserName(String userName, String userNickname) {
        this.userName = userName;
        this.userNickname = userNickname;
        updateTimestamp();
    }

    @Override
    public String toString() {return "User ---- " +
            "[id=" + id +
            "] [userName='" + userName + '\'' +
            "] [userNickname='" + userNickname + '\'' +
            "] [createdAt=" + createdAt + '\'' +
            "] [updatedAt=" + updatedAt + '\'' +
            "]";}
}
