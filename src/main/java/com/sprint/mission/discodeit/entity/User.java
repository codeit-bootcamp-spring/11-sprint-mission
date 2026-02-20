package com.sprint.mission.discodeit.entity;

public class User extends Common{
    private String name;
    private String userId;
    private String password;
    private String email;

    public User(String name, String userId, String password, String email) {
        super();
        this.name = name;
        this.userId = userId;
        this.password = password;
        this.email = email;
        // 인자 추가 시 수정
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() { return email; }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
