package com.sprint.mission.discodeit.exception.readStatus;

//존재하지 않는 ID로 ReadStatus를 조회, 수정, 삭제할 때 404
public class ReadStatusNotFoundException extends RuntimeException {
    public ReadStatusNotFoundException(String message) {
        super(message);
    }
}
