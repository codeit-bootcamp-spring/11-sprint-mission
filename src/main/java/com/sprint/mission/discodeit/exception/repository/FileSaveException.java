package com.sprint.mission.discodeit.exception.repository;

import java.nio.file.Path;

public class FileSaveException extends RuntimeException {
    public FileSaveException(Path path, Throwable cause) {
        super("Failed to save: " + path, cause);
    }
}
