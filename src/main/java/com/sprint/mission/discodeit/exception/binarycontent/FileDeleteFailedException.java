package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.domain.BinaryContentException;

import java.util.Map;

public class FileDeleteFailedException extends BinaryContentException {
    public FileDeleteFailedException() {
        super(ErrorCode.FILE_DELETE_FAILED, Map.of());
    }
}
