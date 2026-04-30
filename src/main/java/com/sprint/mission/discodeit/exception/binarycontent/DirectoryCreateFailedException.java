package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.domain.BinaryContentException;

import java.util.Map;

public class DirectoryCreateFailedException extends BinaryContentException {
    public DirectoryCreateFailedException() {
        super(ErrorCode.DIRECTORY_CREATE_FAILED, Map.of());
    }
}
