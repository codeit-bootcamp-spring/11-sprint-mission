package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String username;
    private String email;
    private boolean online;
    private byte[] profileImageContent;
    private String profileImageContentType;

    // 엔티티와 온라인 여부로 초기화하는 생성자
    public UserResponseDto(User user, boolean online) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.online = online;
        this.profileImageContent = user.getProfileImageContent();
        this.profileImageContentType = user.getProfileImageContentType();
    }
}