package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.domain.BinaryContentException;

import java.util.Map;

public class FileLoadFailedException extends BinaryContentException {
    public FileLoadFailedException() {
        super(ErrorCode.FILE_LOAD_FAILED, Map.of());
    }
}
