package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.domain.BinaryContentException;

import java.util.Map;

public class FileSaveFailedException extends BinaryContentException {
    public FileSaveFailedException() {
        super(ErrorCode.FILE_SAVE_FAILED, Map.of());
    }
}
