package com.sprint.mission.discodeit.entity;

import java.util.UUID;

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

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserStatus status;

    public User(String username, String email, String password) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.password = password;
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

    public void updateProfile(BinaryContent profile) {
        this.profile = profile;
    }

    public void updateStatus(UserStatus status) {
        this.status = status;
    }
}