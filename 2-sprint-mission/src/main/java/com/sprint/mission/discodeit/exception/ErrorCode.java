package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  /// ==== Common ====
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-001", "서버 내부 오류가 발생했습니다."),
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-002", "잘못된 입력값입니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-003", "지원하지 않는 HTTP 메서드입니다."),
  API_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-004", "요청하신 API 주소를 찾을 수 없습니다."),
  INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON-005", "입력값의 타입이 올바르지 않습니다."),
  MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON-006", "필수 요청 파라미터가 누락되었습니다."),

  // ==== User ====
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "존재하지 않는 유저입니다."),
  DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "USER-002", "이미 사용 중인 이메일입니다."),
  DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "USER-003", "이미 사용 중인 이름입니다."),

  // ==== UserStatus ====
  USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-STATUS-001", "유저 상태 정보를 찾을 수 없습니다."),
  USER_STATUS_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER-STATUS-002", "해당 유저의 상태 정보가 이미 존재합니다."),

  // ==== Auth ====
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-001", "아이디 또는 비밀번호가 일치하지 않습니다."),

  // ==== Channel ====
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "CHANNEL-001", "존재하지 않는 채널입니다."),
  PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "CHANNEL-002",
      "비공개 채널의 정보는 수정할 수 없습니다."),

  // ==== Message ====
  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE-001", "존재하지 않는 메시지입니다."),

  // ==== ReadStatus ====
  READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "READ-STATUS-001", "존재하지 않는 메시지 수신 정보입니다."),
  READ_STATUS_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "READ-STATUS-002",
      "해당 채널에 이미 유저의 수신 정보가 존재합니다."),

  // ==== File/BinaryContent ====
  FILE_DIRECTORY_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-001",
      "데이터 디렉토리 생성에 실패했습니다."),
  FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-002", "파일 저장에 실패했습니다."),
  FILE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-003", "파일을 읽어오는 데 실패했습니다."),
  FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-004", "파일 삭제에 실패했습니다."),
  FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-005", "파일 업로드 중 오류가 발생했습니다."),
  BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE-006", "존재하지 않는 파일 콘텐츠입니다."),
  FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "FILE-007", "업로드 가능한 파일 용량을 초과했습니다."),
  FILE_DIRECTORY_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-008", "디렉토리를 읽어오는 데 실패했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}