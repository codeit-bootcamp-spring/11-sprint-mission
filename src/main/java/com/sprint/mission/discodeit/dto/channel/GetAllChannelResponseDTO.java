package com.sprint.mission.discodeit.dto.channel;

public record GetAllChannelResponseDTO(
    ChannelResponseDTO channelResponseDTO
) {
    public static GetAllChannelResponseDTO create(
            ChannelResponseDTO channelRespoonseDTO
    ) {
        return new GetAllChannelResponseDTO(
            channelRespoonseDTO
        );
    }
}
