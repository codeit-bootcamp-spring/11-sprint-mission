package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.nio.file.Path;
import java.util.List;

public class FILEMessageService extends FILEServiceSystem implements MessageService {


    private final Path directory = Path.of("src/main/resources/Messages/");

    public FILEMessageService() {
    }

    @Override
    public String sendMessage(String senderId, String channelId, String message) {
        if(isExistMessage(channelId)){
            System.out.println("이미 존재하는 메시지 아이디입니다.");
            return null;

        }

        Message messageEntity = new Message(channelId,senderId,message);
        save(idtoPath(messageEntity.getMessageId()),messageEntity);
        return messageEntity.getMessageId();


    }

    @Override
    public void readMessage(String messageId) {

        Message message = getMessageToId(messageId);

        if(message == null){
            return;
        }

        System.out.println(message);



    }

    @Override
    public void readAllMessage() {

        List<Message> Messages = load(directory);
        Messages.stream()
                .sorted(Message::compareTo)
                .forEach(System.out::println);



    }

    @Override
    public void updateMessage(String messageId, String message) {

        Message messageEntity = getMessageToId(messageId);
        if(messageEntity == null){
            return;
        }

        messageEntity.updateMessage(message);

        save(idtoPath(messageId),messageEntity);

        System.out.println("메시지 내용 변경 완료.");



    }

    @Override
    public void deleteMessage(String messageId) {

        Message message = getMessageToId(messageId);
        if(message == null){
            return;
        }

        delete(idtoPath(messageId));

        System.out.println("메시지 삭제 완료");
    }

    public boolean isExistMessage(String messageId){

        List<Message> Messages = load(directory);

        for(Message message : Messages){

            if(message.getMessageId().equals(messageId)){
                return true;
            }
        }
        return false;





    }


    public Message getMessageToId(String messageId){

        List<Message> Messages = load(directory);

        Message message;

        message = Messages.stream()
                .filter(m -> m.getMessageId().equals(messageId))
                .findFirst()
                .orElse(null);


        return message;



    }

    Path idtoPath(String MessageId){
        return directory.resolve(MessageId+".dat");
    }


}
