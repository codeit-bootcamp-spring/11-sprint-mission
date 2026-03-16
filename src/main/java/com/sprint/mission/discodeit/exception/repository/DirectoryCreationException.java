package com.sprint.mission.discodeit.exception.repository;

import java.nio.file.Path;

public class DirectoryCreationException extends RuntimeException {
    public DirectoryCreationException(Path directory, Throwable cause) {
        super("Failed to create directory: " + directory, cause);
    }
}
