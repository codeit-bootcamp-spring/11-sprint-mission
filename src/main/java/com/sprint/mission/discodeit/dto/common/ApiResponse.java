package com.sprint.mission.discodeit.dto.common;

import com.sprint.mission.discodeit.exception.ErrorResponse;

public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorResponse error
) {
    public static ApiResponse<Void> error(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }
}

