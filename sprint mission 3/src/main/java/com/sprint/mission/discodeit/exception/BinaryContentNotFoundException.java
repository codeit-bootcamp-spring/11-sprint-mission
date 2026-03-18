package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class BinaryContentNotFoundException extends RuntimeException {
    public BinaryContentNotFoundException(UUID id) {
        super("존재하지 않는 파일입니다. id=" + id);
    }
}
