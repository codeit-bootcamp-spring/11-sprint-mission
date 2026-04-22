package com.sprint.mission.discodeit.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PageResponse<T>(
    List<T> content,
    @Schema(type = "string", nullable = true)
    Object nextCursor,
    int size,
    boolean hasNext,
    Long totalElements
) {

}
