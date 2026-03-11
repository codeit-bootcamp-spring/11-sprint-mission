package com.sprint.mission.discodeit.entity;


import java.util.ArrayList;
import java.util.List;

public class User extends Entity{

    private String nickname; //닉네임
    private final String userId; //유저 아이디
    private String password; //비밀번호
    private Status status; // 현재 상태(온라인, 부재중), 현재 구현에서는 의미 없음
    private final List<Message>  defaultMessages; //채널 입장시 디폴트 메시지(어느 채널 소속 체크용)


    public enum Status {
        ACTIVE, INACTIVE
    }

    public User(String userId, String password, String nickname) {
        super();
        this.password = password;
        this.userId = userId;
        this.nickname = nickname;
        defaultMessages = new ArrayList<>();
        status = Status.ACTIVE;
    }

    public Status getStatus() {
        return status;
    }




    public String getNickname() {
        return nickname;
    }

    public String getUserId() {
        return userId;
    }


    // 업데이트 되는 모든 유저 필드는 비밀번호를 필요
    public boolean updateStatus(Status status, String password){

        if(!checkSamePassword(password))
            return false;
        this.status = status;
        super.updateUpdatedAt();
        return true;
    }
    public boolean updatePassword(String oldPassword, String newPassword) {


        if(!checkSamePassword(oldPassword))
            return false;

        this.password = newPassword;
        super.updateUpdatedAt();
        return true;

    }


    public boolean updateNickname(String nickname, String password) {

        if(!checkSamePassword(password))
            return false;

        this.nickname = nickname;
        super.updateUpdatedAt();
        return true;


    }

    public List<Message> getDefaultMessages() {
        return defaultMessages;
    }

    public void addDefaultMessage(Message message){
        defaultMessages.add(message);
    }

    public boolean checkSamePassword(String password){

        return this.password.equals(password);
    }

    @Override
    public String toString() {
        return "User{" +
                "nickname='" + nickname + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +

                '}';
    }
}
