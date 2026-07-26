package com.sprint.mission.discodeit.realtime;

import com.sprint.mission.discodeit.dto.SseMessage;
import com.sprint.mission.discodeit.dto.WebSocketMessage;

public interface RealtimeDispatcher {

  void dispatch(WebSocketMessage message);

  void dispatch(SseMessage message);
}