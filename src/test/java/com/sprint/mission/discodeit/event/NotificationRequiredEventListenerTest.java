package com.sprint.mission.discodeit.event;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationRequiredEventListenerTest {

  @Spy
  private NotificationRequiredEventListener listener = new NotificationRequiredEventListener();

  @Test
  @DisplayName("S3 업로드 실패 이벤트 발생 시 예외 없이 처리된다")
  void on_S3UploadFailed_HandledWithoutException() {
    // given
    S3UploadFailedEvent event = new S3UploadFailedEvent(
        UUID.randomUUID().toString(),
        UUID.randomUUID(),
        "Connection timeout"
    );

    // when & then
    listener.on(event);
    verify(listener).on(event);
  }
}
