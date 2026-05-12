package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.domain.ChannelException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChannelParticipantDuplicatedException extends ChannelException {
    public ChannelParticipantDuplicatedException(List<UUID> participantIds) {
        super(ErrorCode.CHANNEL_PARTICIPANT_DUPLICATED, Map.of("participantIds", participantIds));
    }
}
