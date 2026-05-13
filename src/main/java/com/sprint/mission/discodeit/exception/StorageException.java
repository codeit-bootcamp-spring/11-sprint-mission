package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.UUID;

public class StorageException extends DiscodeitException {

    public StorageException(String message) {
        super(ErrorCode.FILE_STORAGE_ERROR, Map.of("reason", message));
    }

    public StorageException(String message, Throwable cause) {
        super(ErrorCode.FILE_STORAGE_ERROR, Map.of("reason", message), cause);
    }

    public StorageException(UUID binaryContentId, Throwable cause) {
        super(
                ErrorCode.FILE_STORAGE_ERROR,
                Map.of("binaryContentId", binaryContentId),
                cause
        );
    }
}