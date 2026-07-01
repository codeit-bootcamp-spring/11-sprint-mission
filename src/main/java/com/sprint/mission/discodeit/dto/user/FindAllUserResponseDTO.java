package com.sprint.mission.discodeit.dto.user;

import java.util.List;

public record FindAllUserResponseDTO(
        List<FindUserByIdResponseDTO> userList
) {
    public static FindAllUserResponseDTO from(List<FindUserByIdResponseDTO> userList) {
        return new FindAllUserResponseDTO(
                userList
        );
    }
}
