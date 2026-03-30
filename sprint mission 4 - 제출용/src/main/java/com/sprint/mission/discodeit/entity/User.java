package com.sprint.mission.discodeit.entity;
import com.sprint.mission.discodeit.exception.DiscodeitException;
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
