package com.sprint.mission.discodeit.event.binarycontent;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.event.CreatedEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

// BinaryContentStorage.put 메서드를 트랜잭션에서 분리시키기 위한 객체
// put 매개변수는 UUID, byte[] type이기 때문에 이벤트 매개변수에 해당 타입들을 적용
@Getter
public class BinaryContentCreatedEvent extends CreatedEvent<BinaryContent> {

  private final byte[] bytes;
  private final UUID channelId;
  private final UUID receiverId;

  public BinaryContentCreatedEvent(
      BinaryContent data,
      Instant createdAt,
      byte[] bytes,
      UUID channelId,
      UUID receiverId) {
    super(data, createdAt);
    this.bytes = bytes;
    this.channelId = channelId;
    this.receiverId = receiverId;
  }
}
