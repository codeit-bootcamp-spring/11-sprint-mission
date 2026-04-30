package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.domain.ChannelException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InvalidParticipantIdException extends ChannelException {
    public InvalidParticipantIdException(List<UUID> participantIds) {
        super(ErrorCode.CHANNEL_INVALID_PARTICIPANT_ID, Map.of("participantIds", participantIds));
    }
}
