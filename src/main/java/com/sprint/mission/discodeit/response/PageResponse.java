package com.sprint.mission.discodeit.response;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    Object nextCursor,
    int size,
    boolean hasNext,
    Long totalElements // 값이 없을 수도 있으므로 기본형(long)이 아닌 참조형(Long) 사용
) {

}
