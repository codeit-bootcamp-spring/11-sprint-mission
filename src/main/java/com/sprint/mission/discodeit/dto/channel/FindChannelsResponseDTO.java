package com.sprint.mission.discodeit.dto.channel;

import java.util.List;

public record FindChannelsResponseDTO(
        List<ChannelResponseDTO> list
) {
    public static FindChannelsResponseDTO from(List<ChannelResponseDTO> list) {
        return new FindChannelsResponseDTO(
                list
        );
    }
}
