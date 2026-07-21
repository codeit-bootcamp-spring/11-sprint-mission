package com.sprint.mission.discodeit.event.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BinaryContentEventListenerTest {

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @Mock
  private BinaryContentService binaryContentService;

  @InjectMocks
  private BinaryContentEventListener listener;

  private UUID binaryContentId;
  private byte[] bytes;

  @BeforeEach
  void setUp() {
    binaryContentId = UUID.randomUUID();
    bytes = "test data".getBytes();
  }

  @Test
  @DisplayName("바이너리 데이터 저장 성공 시 상태를 SUCCESS로 갱신한다")
  void handleBinaryContentCreated_Success() {
    // given
    BinaryContentCreatedEvent event = new BinaryContentCreatedEvent(binaryContentId, bytes);

    // when
    listener.handleBinaryContentCreated(event);

    // then
    verify(binaryContentStorage).put(binaryContentId, bytes);
    verify(binaryContentService).updateStatus(binaryContentId, BinaryContentStatus.SUCCESS);
  }

  @Test
  @DisplayName("바이너리 데이터 저장 실패 시 상태를 FAIL로 갱신한다")
  void handleBinaryContentCreated_Failure() {
    // given
    BinaryContentCreatedEvent event = new BinaryContentCreatedEvent(binaryContentId, bytes);
    willThrow(new RuntimeException("storage error"))
        .given(binaryContentStorage).put(any(UUID.class), any(byte[].class));

    // when
    listener.handleBinaryContentCreated(event);

    // then
    verify(binaryContentService).updateStatus(binaryContentId, BinaryContentStatus.FAIL);
  }
}
