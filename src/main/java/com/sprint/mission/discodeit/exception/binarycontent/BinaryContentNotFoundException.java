package com.sprint.mission.discodeit.exception.binarycontent;

import java.util.UUID;

public class BinaryContentNotFoundException extends RuntimeException {
    public BinaryContentNotFoundException(UUID id) {
        super("BinaryContent not found: " + id);
    }
}
