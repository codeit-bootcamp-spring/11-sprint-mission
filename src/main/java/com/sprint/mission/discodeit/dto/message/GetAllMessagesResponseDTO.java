package com.sprint.mission.discodeit.dto.message;

import java.util.List;

public record GetAllMessagesResponseDTO(
        List<MessageResponseDTO> list
) {
    public static GetAllMessagesResponseDTO from(List<MessageResponseDTO> list) {
        return new GetAllMessagesResponseDTO(
                list
        );
    }
}
