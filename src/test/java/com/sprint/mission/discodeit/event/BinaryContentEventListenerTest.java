package com.sprint.mission.discodeit.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.UUID;
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

  @Test
  @DisplayName("파일 저장 성공 시 status가 SUCCESS로 업데이트된다")
  void on_Success() {
    // given
    UUID id = UUID.randomUUID();
    byte[] bytes = "file content".getBytes();
    BinaryContentCreatedEvent event = new BinaryContentCreatedEvent(id, bytes);

    given(binaryContentStorage.put(id, bytes)).willReturn(id);

    // when
    listener.on(event);

    // then
    verify(binaryContentStorage).put(id, bytes);
    verify(binaryContentService).updateStatus(id, BinaryContentStatus.SUCCESS);
    verify(binaryContentService, never()).updateStatus(id, BinaryContentStatus.FAIL);
  }

  @Test
  @DisplayName("파일 저장 실패 시 status가 FAIL로 업데이트된다")
  void on_StorageFailure() {
    // given
    UUID id = UUID.randomUUID();
    byte[] bytes = "file content".getBytes();
    BinaryContentCreatedEvent event = new BinaryContentCreatedEvent(id, bytes);

    willThrow(new RuntimeException("storage error")).given(binaryContentStorage).put(id, bytes);

    // when
    listener.on(event);

    // then
    verify(binaryContentStorage).put(id, bytes);
    verify(binaryContentService).updateStatus(id, BinaryContentStatus.FAIL);
    verify(binaryContentService, never()).updateStatus(id, BinaryContentStatus.SUCCESS);
  }

  @Test
  @DisplayName("파일 저장은 정확히 한 번만 시도한다")
  void on_PutCalledExactlyOnce() {
    // given
    UUID id = UUID.randomUUID();
    byte[] bytes = "file content".getBytes();
    BinaryContentCreatedEvent event = new BinaryContentCreatedEvent(id, bytes);

    given(binaryContentStorage.put(id, bytes)).willReturn(id);

    // when
    listener.on(event);

    // then
    verify(binaryContentStorage).put(eq(id), eq(bytes));
  }
}
