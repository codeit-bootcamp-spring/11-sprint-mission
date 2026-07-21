package com.sprint.mission.discodeit.event;

import java.util.UUID;

/**
 * BinaryContent의 메타 정보가 DB에 저장 완료되었음을 의미하는 이벤트입니다.
 * 이 이벤트를 수신하는 리스너가 실제 바이너리 데이터를 스토리지에 저장합니다.
 */
public record BinaryContentCreatedEvent(
    UUID binaryContentId,
    byte[] bytes
) {

}
