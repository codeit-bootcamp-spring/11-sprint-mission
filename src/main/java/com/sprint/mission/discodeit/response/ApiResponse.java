package com.sprint.mission.discodeit.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final int status;
    private final String message;
    private final T data;

    // 데이터가 포함된 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "SUCCESS", data);
    }

    // 데이터가 없는 성공 응답
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "SUCCESS", null);
    }

    // 에러 응답
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
