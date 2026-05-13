package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiscodeitException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Instant timestamp;
    private final Map<String, Object> details = new LinkedHashMap<>();

    public DiscodeitException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getDetails() {
        return Collections.unmodifiableMap(details);
    }

    public DiscodeitException addDetail(String key, Object value) {
        details.put(key, value);
        return this;
    }
}
