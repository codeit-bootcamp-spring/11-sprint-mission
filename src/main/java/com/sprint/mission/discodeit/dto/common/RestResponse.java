package com.sprint.mission.discodeit.dto.common;

import com.sprint.mission.discodeit.exception.ErrorResponse;

public record RestResponse<T>(
    boolean success,
    T data,
    ErrorResponse error
) {

  public static <T> RestResponse<T> ok(T data) {
    return new RestResponse<>(true, data, null);
  }

  public static RestResponse<Void> error(ErrorResponse error) {
    return new RestResponse<>(false, null, error);
  }
}