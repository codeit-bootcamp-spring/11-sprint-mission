package com.sprint.mission.discodeit.entity.Domain;

import java.io.Serializable;

public class User extends BaseEntity implements Serializable {
    private String userName;
    private String userNickname;
    private String userStatus;
    private static final long serialVersionUID = 1L;

    public User(String userName, String userNickname, String userStatus) {
        super();
        this.userName = userName;
        this.userNickname = userNickname;
        this.userStatus = userStatus;
    }

    public String getUserName() {return userName;}
    public String getUserNickname() {return userNickname;}
    public String getStatus() {return userStatus;}

    public void updateUserName(String userName, String userNickname, String userStatus) {
        this.userName = userName;
        this.userNickname = userNickname;
        this.userStatus = userStatus;
        updateTimestamp();
    }

    @Override
    public String toString() {return "User ---- " +
            "[id=" + id +
            "] [userName='" + userName + '\'' +
            "] [userNickname='" + userNickname + '\'' +
            "] [userStatus='" + userStatus + '\'' +
            "] [createdAt=" + createdAt + '\'' +
            "] [updatedAt=" + updatedAt + '\'' +
            "]";}
}
