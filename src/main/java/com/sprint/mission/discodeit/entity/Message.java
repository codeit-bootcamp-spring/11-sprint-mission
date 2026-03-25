package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Message implements Serializable {

    // 객체 직렬화
    private static final long serialVersionUID = 1L;

    // 필수
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;

    // 어떤 채널의 멤버가 메시지를 작성하였는가
    private String content; // 메시지 내용

    // 연관 관계 필드
    private final UUID channelId; // 어떤 채널에 들어가는 메시지인지, Channel과 연결됨 / Channel의 UUID id
    private final UUID authorId; // 어떤 유저가 작성한 메시지인지, User과 연결됨 / User의 UUID id
    private List<UUID> attachmentIds; // BinaryContent의 UUID id

    // 정적 팩토리 메서드
    private Message(String content, UUID channelId, UUID authorId) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();;
        this.updatedAt = this.createdAt;
        this.content = content;
        this.channelId = channelId;
        this.authorId = authorId;
        this.attachmentIds = new ArrayList<>(); // 삽입/삭제보다 조회가 더 많이 일어나기 때문에 LinkedList가 아닌 ArrayList 사용
    }

    // 정적 팩토리 메서드
    public static Message create(String content, UUID channelId, UUID authorId) {
        return new Message(content, channelId, authorId);
    }

    // getter(Lombok의 @Getter로 대체)

    // update
    private void update() {
        this.updatedAt = Instant.now();;
    }
    public void updateContent(String content) {
        this.content = content;
        update();
    }

    @Override
    public String toString() {
        return "메시지 UUID : " + id
                + "\n 메시지 생성 시간 : " + createdAt
                + ", 메시지 수정 시간 : " + updatedAt
                + "\n 메시지 내용 : " + content
                + "\n 메시지가 작성된 채널 ID : " + channelId
                + ", 메시지 작성자 ID : " + authorId;
    }
}