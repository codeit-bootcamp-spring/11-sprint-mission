package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class JCFMessageService implements MessageService {

    private final Map<String, Message> data;
    UserService userService;
    ChannelService channelService;

    public JCFMessageService(UserService userService, ChannelService channelService){

     data = new HashMap<>();
     this.userService = userService;
     this.channelService = channelService;


    }

    @Override
    public String sendMessage(String senderId, String channelId, String message) {

        Message messageEntity = new Message(senderId,channelId,message);


        if(!channelService.isExistChannel(channelId)){
            System.out.println("존재하지 않는 채널입니다.");
            return null;
        }

        if(!channelService.isChannelsMember(channelId,senderId)){
            System.out.println("해당 채널에 존재하지 않는 멤버입니다.");
            return null;
        }




        data.put(messageEntity.getMessageId(),messageEntity);

        String messageID = messageEntity.getMessageId();

        System.out.println("\"" + message + "\"" + " 메시지를 채널에 보냈습니다.");
        return messageID;

    }

    @Override
    public void readMessage(String messageId) {

        if(isExistMessage(messageId)){
            System.out.println(data.get(messageId));
            return;
        }
        System.out.println("존재하지 않는 메시지 아이디 입니다.");

    }

    @Override
    public void readAllMessage() {

        data.values().stream()
                .sorted(Message::compareTo)
                .forEach(System.out::println);



    }

    @Override
    public void updateMessage(String messageId, String message) {

        if(isExistMessage(messageId)){

            String oldMessage;
            Message messageEntity = data.get(messageId);
            oldMessage = messageEntity.getMessage();

            messageEntity.updateMessage(message);
            System.out.println("메시지 수정 완료! " + oldMessage + " -> " + message);
            return;


        }
        System.out.println("존재하지 않는 메시지 아이디 입니다.");
    }

    @Override
    public void deleteMessage(String messageId) {


        if(isExistMessage(messageId)){

            String message = data.get(messageId).getMessage();
            data.remove(messageId);
            System.out.println( "\"" +message + "\" 메세지 삭제 완료");
            return;
        }
        System.out.println("존재하지 않는 메시지 아이디 입니다");

    }

    public boolean isExistMessage(String messageId){

        return data.get(messageId) != null;


    }
}
