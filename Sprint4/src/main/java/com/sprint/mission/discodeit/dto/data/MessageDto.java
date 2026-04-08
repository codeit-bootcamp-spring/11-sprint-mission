package com.sprint.mission.discodeit.dto.data;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public record MessageDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String content,
        UUID authorId,
        UUID channelId,
        List<UUID> binaryContentIds // 요구사항: 일대다 관계 처리용
) {}