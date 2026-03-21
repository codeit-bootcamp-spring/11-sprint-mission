package com.sprint.mission.discodeit.exception.repository;

import java.nio.file.Path;

public class FileDeleteException extends RuntimeException {
    public FileDeleteException(Path path, Throwable cause) {
        super("Failed to delete: " + path, cause);
    }
}
