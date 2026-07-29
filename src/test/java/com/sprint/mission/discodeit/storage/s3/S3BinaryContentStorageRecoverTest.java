package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.StorageException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class S3BinaryContentStorageRecoverTest {

    private final ApplicationEventPublisher eventPublisher =
            mock(ApplicationEventPublisher.class);
    private final S3BinaryContentStorage storage = new S3BinaryContentStorage(
            "access-key",
            "secret-key",
            "ap-northeast-2",
            "bucket",
            600,
            eventPublisher
    );

    @AfterEach
    void tearDown() {
        MDC.clear();
        storage.close();
    }

    @Test
    void recover_publishesFailureEventAndRethrowsException() {
        // given
        UUID binaryContentId = UUID.randomUUID();
        StorageException exception = new StorageException(
                "upload failed",
                new IllegalStateException("S3 error")
        );
        MDC.put("requestId", "request-id");

        // when & then
        assertThatThrownBy(() -> storage.recover(
                exception,
                binaryContentId,
                new byte[0]
        )).isSameAs(exception);

        ArgumentCaptor<S3UploadFailedEvent> eventCaptor =
                ArgumentCaptor.forClass(S3UploadFailedEvent.class);
        then(eventPublisher).should().publishEvent(eventCaptor.capture());

        S3UploadFailedEvent event = eventCaptor.getValue();
        assertThat(event.taskName()).isEqualTo("BinaryContentUpload");
        assertThat(event.requestId()).isEqualTo("request-id");
        assertThat(event.binaryContentId()).isEqualTo(binaryContentId);
        assertThat(event.errorMessage()).isEqualTo("S3 error");
    }
}
