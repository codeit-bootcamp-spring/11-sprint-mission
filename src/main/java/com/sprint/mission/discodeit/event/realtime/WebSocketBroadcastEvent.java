package com.sprint.mission.discodeit.event.realtime;

import java.io.Serializable;

public record WebSocketBroadcastEvent(
    String destination,
    Object payload
) implements Serializable {

}