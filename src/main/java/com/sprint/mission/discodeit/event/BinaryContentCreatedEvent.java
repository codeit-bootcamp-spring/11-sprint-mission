package com.sprint.mission.discodeit.event;

import java.util.UUID;

// BinaryContentStorage.put 메서드를 트랜잭션에서 분리시키기 위한 객체
// put 매개변수는 UUID, byte[] type이기 때문에 이벤트 매개변수에 해당 타입들을 적용
public record BinaryContentCreatedEvent(
    UUID binaryContentId,
    byte[] bytes
) {

}
