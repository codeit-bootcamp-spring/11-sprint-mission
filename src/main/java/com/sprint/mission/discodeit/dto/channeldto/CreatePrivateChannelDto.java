package com.sprint.mission.discodeit.dto.channeldto;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public record CreatePrivateChannelDto(

       UUID ownerId,
       List<UUID> membersId

) {
}
