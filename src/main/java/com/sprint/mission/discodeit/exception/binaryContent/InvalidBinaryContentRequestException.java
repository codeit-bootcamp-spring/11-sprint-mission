package com.sprint.mission.discodeit.exception.binaryContent;

//업로드한 파일 용량이 0바이트거나, 파일명/확장자가 누락되었을 때
public class InvalidBinaryContentRequestException extends RuntimeException {
    public InvalidBinaryContentRequestException(String message) { super(message); }
}
