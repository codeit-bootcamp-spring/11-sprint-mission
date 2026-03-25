package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public class UserDto {

    @Builder
    public record CreateRequest(
            String userName,
            String password,
            String email,
            String nickname,
            String description,
            UUID profileImageId // 선택적으로 프로필 이미지 등록
    ) {
        // DTO -> Entity
        public User toEntity() {
            return User.builder()
                    .userName(this.userName)
                    .password(this.password)
                    .email(this.email)
                    .nickname(this.nickname)
                    .description(this.description)
                    .profileImageId(this.profileImageId)
                    .build();
        }
    }

    public record UpdateRequest(
            String username,
            String nickname,
            String description,
            String email,
            String password,
            UUID profileImageId
    ) {}

    @Builder
    public record Response(
            UUID id,
            String username,
            String nickname,    // 추가
            String email,
            String description, // 추가
            UUID profileImageId, // 추가
            boolean isOnline,   // 핵심!
            Instant lastActivityAt // 추가
    ) {

        // Entity -> DTO
        public static Response of(User user, UserStatus status) {
            return Response.builder()
                    .id(user.getId())
                    .username(user.getUserName())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .description(user.getDescription())
                    .profileImageId(user.getProfileImageId())
                    .isOnline(status.isOnline())
                    .lastActivityAt(status.getLastActiveAt())
                    .build();
        }
    }
}