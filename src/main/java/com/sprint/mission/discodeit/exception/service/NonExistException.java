package com.sprint.mission.discodeit.exception.service;

public class NonExistException extends RuntimeException {
    public NonExistException(String message) {
        super(message);
    }
}
