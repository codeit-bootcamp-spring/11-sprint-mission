package com.sprint.mission.discodeit.entity;

public class Message extends Entity {

    private String message;
    private final String messageId; //메시지 아이디 ("msg-" + UUID)
    private final String senderId; //전송자 아이디
    private final String channelId; //채널 아이디
    private messageStatus status; // 활성화, 비활성화 상태


    public enum messageStatus { //
        ACTIVE, INACTIVE
    }

    public Message(String senderId, String channelId, String message) {

        super();
        messageId = "msg-" + super.getId();
        this.senderId = senderId;
        this.channelId = channelId;
        this.message = message;
        status = messageStatus.ACTIVE;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void updateMessage(String message){

        this.message = message;
        super.updateUpdatedAt();
    }

    public messageStatus getStatus() {
        return status;
    }

    public void setStatus(messageStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", messageID='" + messageId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", channelId='" + channelId + '\'' +
                '}';
    }
}
