package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@Profile("service-jcf")
@RequiredArgsConstructor
public class JCFChannelService implements ChannelService {

    private final Map<UUID, Channel> channelData = new HashMap<>();

    // 의존
    private MessageService messageService;
    private UserService userService;

    //생성자
    public JCFChannelService(){}
    /*public JCFChannelService(MessageService messageService){
        this.messageService = messageService;
    }
    public JCFChannelService(UserService userService){
        this.userService = userService;
    }
    public JCFChannelService(MessageService messageService, UserService userService){
        this.messageService = messageService;
        this.userService = userService;
    }*/

    //setter
    @Override
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
    @Override
    public void setUserService(UserService userService){
        this.userService = userService;
    }

    @Override
    public void create(Channel channel) {
        if(channel.getAdminId() == null){
            System.out.println("어드민 id가 유효하지 않습니다.");
            return;
        }
        if(!channelData.containsKey(channel.getId())) {
            //System.out.println("채널 등록 완료\n"+channel);
            channelData.put(channel.getId(), channel);
            userService.read(channel.getAdminId()).joinChannel(channel.getId());
        } else System.out.println("이미 생성된 채널입니다");
    }

    @Override
    public Channel read(UUID channelId) { //public Optional<Channel> read(UUID channelId)??
        if(channelData.containsKey(channelId)){
            return channelData.get(channelId);
        } else {
            System.out.println("존재하지 않는 채널입니다.");
            return null;
            // Optional.ofNullable(channelData.get(channelId)); 이게 더 안정적?
        }

    }

    @Override
    public List<Channel> readAll() {
        if(!channelData.isEmpty()){
            return new ArrayList<>(channelData.values());
        } else {
            System.out.println("채널이 존재하지 않습니다.");
            return null;
        }
    }

    @Override
    public void save(Channel channel) {
        if(channelData.containsKey(channel.getId())){
            channelData.put(channel.getId(), channel);
        } else {
            System.out.println("존재하지 않는 채널입니다.");
        }
    }

    @Override
    public void delete(UUID channelId) {
        if(channelId == null){
            System.out.println("존재하지 않는 채널입니다.");
            return;
        }
        if(!channelData.containsKey(channelId)){
            System.out.println("존재하지 않는 채널입니다.");
            return;
        }
        //채널에 속한 유저들의 속한채널 리스트에서 이 채널을 제외
            //스트림으로 이 채널의 멤버 리스트 불러 온다음 각 유저의 속한 채널 리스트에 접근해서 이 채널id를 통해 삭제
        Stream<UUID> disconnectMembers = channelData.get(channelId).getMemberId().stream();
        disconnectMembers.forEach(userId -> {

            if(userId != null){ // null 고려
                User user = userService.read(userId);
                if(user != null){
                    user.leaveChannel(channelId);
                    userService.save(user); // 변경사항 저장 꼭 해야함
                }
            }
        });
        //이 채널에서 작성된 메시지 일괄 삭제
        messageService.clearMessagesInChannel(channelId);
        //최종 채널 삭제
        channelData.remove(channelId);
    }
    @Override
    public void deleteChannelByAdmin(UUID adminId){

        List<UUID> channelsToDelete = channelData.values().stream()
                .filter(channel -> channel.getAdminId().equals(adminId))
                .map(Channel::getId)
                .toList();

        for(UUID channelId : channelsToDelete){
            this.delete(channelId);
        }
    }

    @Override
    public void addUserToChannel(UUID userId, UUID channelId) {
        if(userId == null && channelId == null) {
            System.out.println("id가 null입니다");
            return;
        }
        Channel channel = channelData.get(channelId);
        User user = userService.read(userId);
        if(channel != null && user != null){
            channelData.get(channelId).addMember(userId);
            userService.read(userId).joinChannel(channelId);
        } else System.out.println("채널이나 유저가 존재하지 않습니다.");
    }

    @Override
    public void removeUserFromChannel(UUID userId, UUID channelId) {
        if(userId == null && channelId == null) {
            System.out.println("id가 null입니다");
            return;
        }
        Channel channel = channelData.get(channelId);
        User user = userService.read(userId);
        if(channel != null && user != null){
            channelData.get(channelId).removeMember(userId);
            userService.read(userId).leaveChannel(channelId);
        } else System.out.println("채널이나 유저가 존재하지 않습니다.");

    }
}
