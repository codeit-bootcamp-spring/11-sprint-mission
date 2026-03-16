package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class ReadStatusNotFoundException extends RuntimeException {
    public ReadStatusNotFoundException(UUID id) {
        super("ReadStatus not found: " + id);
    }
}
