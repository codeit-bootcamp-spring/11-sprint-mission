package com.sprint.mission.discodeit.dto.channeldto.request;

import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(

    List<UUID> participantIds

) {

}
