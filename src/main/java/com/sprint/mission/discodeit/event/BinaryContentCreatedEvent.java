package com.sprint.mission.discodeit.event;

import java.util.UUID;

/**
 * BinaryContent 메타 정보가 DB에 정상적으로 저장되었음을 의미하는 이벤트.
 * 이 이벤트를 수신하면 실제 바이너리 데이터(byte[])를 스토리지에 저장한다.
 */
public record BinaryContentCreatedEvent(
    UUID binaryContentId,
    byte[] bytes
) {

}
