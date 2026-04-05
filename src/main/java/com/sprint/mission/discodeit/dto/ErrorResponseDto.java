package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    private int status;
    private String error;
    private String message;
}
