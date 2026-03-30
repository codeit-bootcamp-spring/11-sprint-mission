package com.sprint.mission.dicordeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class User implements Serializable {
    private String username, password, email;
    private UUID id;
    private long createdAt;
    private long updatedAt;

    public long getUpdatedAt() {
        return updatedAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        //위에 입력한 정보를 저장해주는 id 생성
        this.id = UUID.randomUUID();
        //id를 생성한 시간 체크
        this.createdAt = System.currentTimeMillis();
        //id를 수정한 시간을 생성한 시간으로 교체
        this.updatedAt = this.createdAt;

    }

    // 필드를 수정하는 update 함수
    public void update(String newPassword, String newEmail) {
        // 1. 내 비밀번호(this.password)를 새로 받은 비밀번호(newPassword)로 바꾼다.
        this.password = newPassword;
        // 2. 내 이메일(this.email)을 새로 받은 이메일(newEmail)로 바꾼다.
        this.email = newEmail;
        // 3. 정보가 수정되었으니, 수정 시간(this.updatedAt)을 "지금 이 시간"으로 다시 세팅해 준다!
        this.updatedAt = System.currentTimeMillis();
        // 힌트: 아까 생성자에서 현재 시간 구했던 그 코드를 여기에 한 번 더 쓰면 됩니다.
        this.createdAt = System.currentTimeMillis();
    }

    public String getName() {
        return username;

    }
}