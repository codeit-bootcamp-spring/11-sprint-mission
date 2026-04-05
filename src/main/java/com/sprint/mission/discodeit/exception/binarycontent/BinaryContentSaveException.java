package com.sprint.mission.discodeit.exception.binarycontent;

public class BinaryContentSaveException extends RuntimeException {
    public BinaryContentSaveException(String fileName, Throwable cause) {
        super("첨부 파일 저장에 실패했습니다: "+ fileName, cause);
    }
}
