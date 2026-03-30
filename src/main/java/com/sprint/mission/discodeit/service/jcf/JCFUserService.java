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
public class JCFUserService implements UserService {

    private final Map<UUID, User> userData = new HashMap<>();

    // 의존
    private MessageService messageService;
    private ChannelService channelService;

    //생성자?
    public JCFUserService(){}

    //setter
    @Override
    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }
    @Override
    public void setMessageService(MessageService messageService){
        this.messageService = messageService;
    }

    @Override
    public void create(User user) {
        if(!userData.containsKey(user.getId())) { //조건 설정 뭘로 해야하지
            //System.out.println("유저 등록 완료\n"+user);
            userData.put(user.getId(), user);

        } else System.out.println("이미 생성된 유저입니다"); // ?
    }

    @Override
    public User read(UUID userId) {
        if(userData.containsKey(userId)){
            return userData.get(userId);
        } else {
            System.out.println("존재하지 않는 유저입니다.");
            return null;
        }

    }

    @Override
    public List<User> readAll() {
        if(!userData.isEmpty()){
            return new ArrayList<>(userData.values());
        } else {
            System.out.println("유저가 존재하지 않습니다.");
            return null;
        }
    }

    @Override
    public void save(User user) {
        if(userData.containsKey(user.getId())){
            userData.put(user.getId(), user);
        } else {
            System.out.println("존재하지 않는 유저입니다.");
        }
    }

    @Override
    public void delete(UUID userId) {
        if(userData.containsKey(userId)){
            // 유저가 작성한 메시지도 삭제(Message)
            messageService.clearMessagesByUser(userId);
            // 유저가 속한 채널에서 유저 제외(ChannelMember)
            // 오류 발생했던이유: 위에서 삭제될 유저가 관리자인 채널을 지웠는데 유저가 속한 채널 목록에는 그 채널이 남아서 null발생
            Stream<UUID> disconnectChannels = userData.get(userId).getJoinedChannelId().stream();
            disconnectChannels.forEach( channelId ->{
                if(channelId != null){
                    Channel channel = channelService.read(channelId);
                    if(channel != null){
                        channel.removeMember(userId);
                        channelService.save(channel);
                    }
                }
            });
            // 유저가 관리자인 채널을 삭제(Channel), 위에서 유저가 속한 채널의 멤버 목록에서 이미 지워졌으므로,
            // 지금 단계에서는 유저는 그 채널의 관리자이지만 멤버 목록에는 없는 상태
            channelService.deleteChannelByAdmin(userId);
            //유저 최종 삭제
            userData.remove(userId);
        } else System.out.println("존재하지 않는 유저입니다.");
    }
}
