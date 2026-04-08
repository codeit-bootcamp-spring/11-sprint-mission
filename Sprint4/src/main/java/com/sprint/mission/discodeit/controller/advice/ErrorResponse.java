package com.sprint.mission.discodeit.controller.advice;

/**
 * 전역 예외 발생 시 반환할 공통 응답 객체
 * record를 사용하면 getter, 생성자 등을 자동으로 생성해 줍니다.
 */
public record ErrorResponse(
        String code,    // 에러 구분 코드 (예: USER_NOT_FOUND)
        String message  // 사용자에게 보여줄 메시지
) {
}