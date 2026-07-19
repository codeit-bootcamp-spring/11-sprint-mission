package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BinaryContentCreatedEventListenerTest {

    @Mock
    private BinaryContentStorage binaryContentStorage;

    @Mock
    private BinaryContentService binaryContentService;

    @InjectMocks
    private BinaryContentCreatedEventListener binaryContentCreatedEventListener;

    @Test
    @DisplayName("이벤트 수신 시 BinaryContentStorage로 바이너리 데이터 저장")
    void on_Success() {
        UUID binaryContentId = UUID.randomUUID();
        byte[] bytes = "test data".getBytes();
        BinaryContentCreatedEvent event = new BinaryContentCreatedEvent(binaryContentId, bytes);

        binaryContentCreatedEventListener.on(event);

        verify(binaryContentStorage).put(eq(binaryContentId), eq(bytes));
        verify(binaryContentService).updateStatus(eq(binaryContentId), eq(BinaryContentStatus.SUCCESS));
    }
}
