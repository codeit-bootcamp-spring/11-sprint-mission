package com.sprint.mission.discodeit.exception;

public record ErrorResponseDto(int status, String error, String message) {

}
