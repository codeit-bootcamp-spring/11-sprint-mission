package com.sprint.mission.discodeit.exception.repository;

import java.nio.file.Path;

public class FileLoadException extends RuntimeException {
    public FileLoadException(Path path, Throwable cause) {
        super("Failed to load: " + path, cause);
    }
}
