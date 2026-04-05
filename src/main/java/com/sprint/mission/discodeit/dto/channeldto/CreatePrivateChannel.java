package com.sprint.mission.discodeit.dto.channeldto;

import java.util.List;
import java.util.UUID;

public record CreatePrivateChannel(

    List<UUID> participantIds

) {

}
