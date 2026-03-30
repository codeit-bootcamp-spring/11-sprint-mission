package com.sprint.mission.discodeit.exception;

import java.time.Instant;

// 생성자, getter, equals(), hashCode(), toString() 자동생성
public record ErrorResponse(
    // 에러 발생 시각
    Instant timestamp,

    // HTTP 상태코드 숫자(404)
    int status,

    // 상태코드 이름(NOT FOUND)
    String error,

    // 구체적 에러메세지
    String message
) {

}
