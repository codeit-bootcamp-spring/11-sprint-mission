package com.sprint.mission.discodeit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record UserDto(
    UUID id,
    String username,
    String email,
    BinaryContentDto profile, // 프로필 이미지
    @JsonProperty("online") boolean online // 유저의 상태(온라인이거나 오프라인), 심화 요구사항 적용(Enum -> boolean)
    // User.Status isStatus // 유저의 상태(온라인이거나 오프라인)
) {

}
