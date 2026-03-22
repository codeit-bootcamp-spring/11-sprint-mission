package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFMessageService implements MessageService {

    private final Map<UUID, Message> messageData = new HashMap<>();

    // 의존
    private UserService userService;
    private ChannelService channelService;

    //생성자
    public JCFMessageService(){}
    /*public JCFMessageService(UserService userService){
        this.userService = userService;
    }
    public JCFMessageService(ChannelService channelService){
        this.channelService = channelService;
    }
    public JCFMessageService(UserService userService, ChannelService channelService){
        this.userService = userService;
        this.channelService = channelService;
    }*/

    //setter
    @Override
    public void setUserService(UserService userService){
        this.userService = userService;
    }
    @Override
    public void setChannelService(ChannelService channelService){
        this.channelService = channelService;
    }


    @Override
    public void create(Message message) {
        if(message.getSenderId() == null){
            System.out.println("유저 id가 유효하지 않습니다.");
            return;
        }
        if(message.getChannelId() == null){
            System.out.println("채널 id가 유효하지 않습니다.");
            return;
        }
        if(!messageData.containsKey(message.getId())) {
            //System.out.println("메시지 등록 완료\n"+channel);
            messageData.put(message.getId(), message);
        } else System.out.println("이미 생성된 메시지입니다");
    }

    @Override
    public Message read(UUID id) {
        if(messageData.containsKey(id)){
            return messageData.get(id);
        } else {
            System.out.println("존재하지 않는 메시지입니다.");
            return null;
        }
    }

    @Override
    public List<Message> readAll() {
        if(!messageData.isEmpty()){
            return new ArrayList<>(messageData.values());
        } else{
            System.out.println("메시지가 존재하지 않습니다.");
            return null;
        }
    }

    @Override
    public void update(UUID messageId, MessageUpdateRequest request) {
        if(messageData.containsKey(message.getId())){
            messageData.put(message.getId(), message);
        } else{
            System.out.println("존재하지 않는 메시지입니다.");
        }
    }

    @Override
    public void delete(UUID id) {
        if(messageData.containsKey(id)){
            messageData.remove(id);
            //머 할라고 했더라
        } else{
            System.out.println("삭제 할 수 없음(존재하지 않는 id)");
        }
    }
    @Override
    public void clearMessagesInChannel(UUID channelId) { //채널 삭제 시 사용, 해당 채널 UUID를 매개변수로 받아서 채널 내 모든 메시지 삭제
        // 채널 삭제 시 모든 메시지를 순회 해야함(비효율적)
        // Channel 클래스 내에 채널에서 작성된 메시지 리스트 만들면 모든 메시지 순회하지 않아도 되지만 메시지 작성시 저장 속도 성능 하락??
        // 채널 삭제가 자주 있는 일이 아니니 이대로 만드는게 낫나?
        messageData.values().removeIf(message -> message.getChannelId().equals(channelId));
    }
    @Override
    public void clearMessagesByUser(UUID userId){
        messageData.values().removeIf(message -> message.getSenderId().equals(userId));
    }

}
