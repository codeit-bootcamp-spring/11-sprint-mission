package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ChannelDto {

    // PUBLIC 채널 생성
    public record CreatePublicRequest(
        String name,
        String description
    ) {
        // DTO -> Entity
        public Channel toEntity() {
            return Channel.builder()
                    .name(this.name)
                    .description(this.description)
                    .type(ChannelType.PUBLIC)
                    .build();
        }

    }

    // PRIVATE 채널 생성
    public record CreatePrivateRequest(
        List<UUID> memberIds // 참여 유저 ID 목록 (name, description은 생략)
    ) {
        // DTO -> Entity
        public Channel toEntity() {
            return Channel.builder()
                    .type(ChannelType.PRIVATE)
                    .build();
        }
    }

    public record UpdateRequest(
            String name,
            String description
    ) {}

    @Builder
    public record Response(
            UUID id,
            String name,
            String description,
            ChannelType type,
            Instant lastMessageAt,
            List<UUID> userIds
    ) {
        // 엔티티와 외부 데이터를 조합하여 Response를 만드는 정적 팩토리 메서드
        public static Response of(Channel channel, Instant lastMessageAt, List<UUID> userIds) {
            return Response.builder()
                    .id(channel.getId())
                    .name(channel.getName())
                    .description(channel.getDescription())
                    .type(channel.getType())
                    .lastMessageAt(lastMessageAt)
                    .userIds(channel.getType() == ChannelType.PRIVATE ? userIds : null)
                    .build();
        }
    }
}