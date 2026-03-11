package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

public class BasicMessageService  implements MessageService {


    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;


    public BasicMessageService(MessageRepository messageRepository, ChannelRepository channelRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String sendMessage(String senderId, String channelId, String message) {

        Channel channel = channelRepository.getChannel(channelId);
        //채널 여부 체크
        if(channel == null){
            System.out.println("존재하지 않는 채널입니다.");
            return null;
        }

        //유효 유저 체크
        if(!channel.getMembers().contains(senderId)){
            System.out.println("해당 채널에 존재하지 않는 유저입니다.");
            return null;
        }

        //메시지 생성
        Message msg = new Message(senderId,channelId,message);

        if(!messageRepository.saveMessage(msg)){
            System.out.println("메시지 저장 중 문제가 발생했습니다.");

        }

        // 출력 메시지
        System.out.println("메시지 보내기 완료!");
        return msg.getMessageId();







    }

    @Override
    public void readMessage(String messageId) {

        //메시지 생성
        Message message = messageRepository.getMessage(messageId);

        //메시지 유효 체크
        if(message == null){
            System.out.println("존재하지 않는 메시지 입니다.");
            return;

        }
        //메시지 출력
        System.out.println(message);



    }

    @Override
    public void readAllMessage() {

        //메시지 리스트 체크
        if(messageRepository.getAllMessage() == null){

            System.out.println("메시지 리스트를 불러오는데 문제가 발생했습니다.");
            return;

        }

        //stream으로 출력
        messageRepository.getAllMessage().stream()
                .sorted(Message::compareTo)
                .forEach(System.out::println);



    }

    @Override
    public void updateMessage(String messageId, String message) {

        Message messageEntity = messageRepository.getMessage(messageId);

        //메시지 유효 체크
        if(messageEntity == null){
            System.out.println("존재하지 않는 메시지 입니다.");
            return;

        }

        //메시지 활성화 체크
        if(messageEntity.getStatus() != Message.messageStatus.ACTIVE){

            System.out.println("비활성화된 메시지 입니다.");
            return;

        }

        //TODO : 메시지 비번 체크

        //메시지 수정
        messageEntity.updateMessage(message);

        //저장소에 반영
        if(!messageRepository.updateMessage(messageEntity)){
            System.out.println("메시지 저장중 문제가 발생했습니다.");
            return;
        }

        //출력 메시지
        System.out.println("메시지 수정 완료!");

    }

    @Override
    public void deleteMessage(String messageId) {

        //메시지 가져오기
        Message messageEntity = messageRepository.getMessage(messageId);

        //메시지 유효 체크
        if(messageEntity == null){
            System.out.println("존재하지 않는 메시지 입니다.");
            return;

        }


        // TODO : 비밀번호 체크 로직

        if(!messageRepository.deleteMessage(messageId)){
            System.out.println("삭제 도중 문제가 발생했습니다.");
            return;

        }
        //출력 메시지
        System.out.println("메시지 삭제 완료!");

    }
}
