package com.sprint.mission.discodeit.dto.error;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ExceptionDto(

        HttpStatus code,
        String message,
        String path,
        LocalDateTime timestamp

) {

    public ExceptionDto(HttpStatus code, String message, String path) {
        this(code, message, path, LocalDateTime.now());
    }

    public static ExceptionDto of(HttpStatus code, String message, String path) {
        return new ExceptionDto(code, message, path);
    }








}
