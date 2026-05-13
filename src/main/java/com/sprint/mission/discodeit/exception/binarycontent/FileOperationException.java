package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.UUID;

public class FileOperationException extends DiscodeitException {

    public FileOperationException() {
        super(ErrorCode.FILE_OPERATION_FAILED);
    }

    public static FileOperationException withId(UUID binaryContentId) {
        FileOperationException exception = new FileOperationException();
        exception.addDetail("binaryContentId", binaryContentId);
        return exception;
    }

    public static FileOperationException withFileName(String fileName) {
        FileOperationException exception = new FileOperationException();
        exception.addDetail("fileName", fileName);
        return exception;
    }
}
