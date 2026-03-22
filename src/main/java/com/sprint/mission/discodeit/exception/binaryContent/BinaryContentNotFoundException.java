package com.sprint.mission.discodeit.exception.binaryContent;


// 존재하지 않는 바이너리 콘텐츠(파일, 이미지 등)를 참조하려고 할 때 404
public class BinaryContentNotFoundException extends RuntimeException {
    public BinaryContentNotFoundException(String message) {
        super(message);
    }
}
