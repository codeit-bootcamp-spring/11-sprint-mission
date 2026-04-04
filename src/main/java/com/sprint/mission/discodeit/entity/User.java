package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class User implements Serializable {

    // 객체 직렬화
    private static final long serialVersionUID = 1L;

    // 필수
    private final UUID id; // 사용자 ID, 값이 변하면 안된다.
    private final Instant createdAt; // 계정 생성 시간, 값이 변할수 없다.
    private Instant updatedAt; // 계정 수정 후 시간, 처음에는 계정 생성 시간과 동일

    // 사용자에 대한 데이터
    private String username; // 사용자 이름
    private String nickname; // 사용자 닉네임, 중복 불가
    private String email; // 사용자 이메일, 중복 불가
    private String phoneNumber; // 사용자 전화번호, 중복 불가
    private Status status; // 디스코드 접속 상태(온라인, 자리비움, 방해 금지, 오프라인)
    private String password;

    // 연관관계 필드
    private UUID profileId; // BinaryContent의 UUID

    // 'id', 'createdAt'는 생성자에서 초기화하세요.
    public User(String username, String nickname,
                String email, String phoneNumber, Status status) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }

    // 정적 팩토리 메서드
    // 생성자 오버로딩하여 코드 탬플릿에 적합한 생성자 생성
    private User(String username, String email, String password) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // 정적 팩토리 메서드
    public static User create(String username, String email, String password) {
        return new User(username, email, password);
    }

    // update메서드
    private void update() {
        this.updatedAt = Instant.now();
    }

    // get메서드(Lombok의 @Getter 사용)

    public void updateName(String username) {
        this.username = username;
        update();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        update();
    }

    public void updateEmail(String email) {
        this.email = email;
        update();
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        update();
    }

    public void updateStatus(Status status) {
        this.status = status;
        update();
    }

    public void updatePassword(String password) {
        this.password = password;
        update();
    }

    public void updateProfileId(UUID profileId) {
        this.profileId = profileId;
        update();
    }

    @Override
    public String toString() {
        return "유저 UUID : " + id
                + "\n 생성 시간 : " + createdAt + ", 최근 수정 시간 : " + updatedAt
                + "\n 유저 이름 : " + username + ", 유저 닉네임 : " + nickname
                + "\n 유저 이메일 : " + email + ", 유저 전화번호 : " + phoneNumber
                + "\n 유저 상태 : " + status.getDescription();
    }


    public enum Status {
        ONLINE("온라인"), AWAY("자리비움"), DO_NOT_DISTURB("방해 금지"), OFFLINE("오프라인");

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}