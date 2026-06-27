package com.sprint.mission.discodeit.entity;

import java.util.UUID;

import com.sprint.mission.discodeit.security.authority.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import com.sprint.mission.discodeit.entity.base.MutableBaseEntity;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends MutableBaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @OneToOne
    @JoinColumn(name = "profile_id")
    private BinaryContent profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    public User(String username, String email, String password) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = UserRole.USER;
    }

    public void update(String newUsername, String newEmail, String newPassword) {
        if (newUsername != null && !newUsername.isBlank()) {
            this.username = newUsername;
        }
        if (newEmail != null && !newEmail.isBlank()) {
            this.email = newEmail;
        }
        if (newPassword != null && !newPassword.isBlank()) {
            this.password = newPassword;
        }
    }

    public static User createAdmin(String username, String email, String password) {
        User user = new User(username, email, password);
        user.role = UserRole.ADMIN;
        return user;
    }

    public void updateProfile(BinaryContent profile) {
        this.profile = profile;
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }
}
