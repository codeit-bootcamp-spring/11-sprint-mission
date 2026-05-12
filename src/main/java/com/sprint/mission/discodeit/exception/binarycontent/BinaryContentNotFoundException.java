package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.domain.BinaryContentException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BinaryContentNotFoundException extends BinaryContentException {
    public BinaryContentNotFoundException(UUID binaryContentId) {
        super(ErrorCode.BINARY_CONTENT_NOT_FOUND, Map.of("binaryContentId", binaryContentId));
    }

    public BinaryContentNotFoundException(List<UUID> binaryContentIds) {
        super(ErrorCode.BINARY_CONTENT_NOT_FOUND, Map.of("binaryContentIds", binaryContentIds));
    }
}
