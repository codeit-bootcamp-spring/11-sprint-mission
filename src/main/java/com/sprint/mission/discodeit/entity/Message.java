package com.sprint.mission.discodeit.entity;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
public class Message implements Serializable {
    private final UUID id;
    private final UUID channelId;
    private final UUID authorId;
    private static final long serialVersionUID = 1L;
    private final long createdAt;
    private long updatedAt;
    private String message;

    // 생성자 초기화
    public Message(UUID channelId, UUID authorId, String message) {
        validateMessage(message);
        this.id = UUID.randomUUID();
        this.channelId = channelId;
        this.authorId = authorId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.message = message;
    }

    // 필드를 수정하는 update 함수
    public void update(String newMessage) {
        validateMessage(newMessage);
        this.message = newMessage;
        this.updatedAt = System.currentTimeMillis();
        System.out.println("메세지가 수정되었습니다. 수정시간: " + getUpdatedAt());
    }

    private void validateMessage(String message) {
        if(message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }
    }


    // 생성시간 반환
    public String getCreatedAt() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault());
        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return formattedDate;
    }

    // 업데이트 시간 반환
    public String getUpdatedAt() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(updatedAt), ZoneId.systemDefault());
        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return formattedDate;
    }


    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "메세지 : " + message + ", 생성 시간 : " + getCreatedAt();
    }
}
