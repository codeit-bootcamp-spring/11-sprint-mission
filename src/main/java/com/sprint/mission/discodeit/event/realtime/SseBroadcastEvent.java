package com.sprint.mission.discodeit.event.realtime;

import com.sprint.mission.discodeit.dto.sse.SseMessage;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record SseBroadcastEvent(
    List<UUID> receiverIds,  // null 또는 빈 리스트 = 전체 브로드캐스트
    SseMessage message
) implements Serializable {

}