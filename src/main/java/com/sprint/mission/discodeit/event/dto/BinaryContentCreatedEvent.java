package com.sprint.mission.discodeit.event.dto;

import java.util.UUID;

public record BinaryContentCreatedEvent(
    UUID id,
    byte[] bytes,
    UUID uploaderId
) {

}
