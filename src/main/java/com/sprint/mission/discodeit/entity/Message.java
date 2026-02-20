package com.sprint.mission.discodeit.entity;

public class Message extends Common{
    private String contents;
    private User sender;
    private Channel channel;

    public Message(String contents, User sender, Channel channel) {
        super();
        this.contents = contents;
        this.sender = sender;
        this.channel = channel;
        // 인자 추가 시 수정
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public User getSender() {
        return sender;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "Message{" +
                "contents='" + contents + '\'' +
                ", sender=" + sender.getName() +
                ", channel=" + channel.getName() +
                '}';
    }
}
