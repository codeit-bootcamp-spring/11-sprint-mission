package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class User extends BaseEntity {

    private String username;
    private String email;
    private String password;
    private UUID profileId;

    private User(String username, String email, String password, UUID profileId) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    protected User(User other) {
        super(other);
        this.username = other.username;
        this.email = other.email;
        this.password = other.password;
        this.profileId = other.profileId;
    }

    @Override
    public User copy() {
        return new User(this);
    }

    public static User create(String username, String email, String password, UUID profileId) {
        return new User(username, email, password, profileId);
    }

    // 도메인 로직
    public void authenticate(String rawPassword) {
        if (!password.equals(rawPassword)) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    public void updateUserInfo(String username, String email, String password, UUID profileId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
        touch();
    }

    public void updateUsername(String username) {
        this.username = username;
        touch();
    }

    public void updateEmail(String email) {
        this.email = email;
        touch();
    }

    public void updatePassword(String password) {
        this.password = password;
        touch();
    }

    public void updateProfileId(UUID profileId) {
        this.profileId = profileId;
        touch();
    }

}
