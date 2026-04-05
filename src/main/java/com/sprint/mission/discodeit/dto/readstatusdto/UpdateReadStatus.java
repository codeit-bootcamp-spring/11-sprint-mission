package com.sprint.mission.discodeit.dto.readstatusdto;

import java.time.Instant;

public record UpdateReadStatus(
    Instant newLastReadAt

) {

}
