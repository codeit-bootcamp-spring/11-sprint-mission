package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.exception.channel.ChannelOperationException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String channelName;
    private UUID adminId;
    private final List<UUID> memberId = new ArrayList<>();

    //생성자
    public Channel(String channelName, UUID adminId){
        super();
        this.channelName = channelName;
        this.adminId = adminId;
        memberId.add(adminId);
    }

    //getter
    public List<UUID> getMemberId() {
        return Collections.unmodifiableList(this.memberId);
    }

    //업데이트 메소드
    public void update(String channelName, UUID adminId) {
        if(StringUtils.hasText(channelName) && adminId != null){ //유효한지 검사
            this.channelName = channelName;
            this.adminId = adminId;
            if(!memberId.contains(adminId)){
                memberId.add(adminId);
            }
            updateTime(); // 업데이트 시간 갱신
        } else {
            throw new ChannelOperationException("채널 정보를 갱신하는데 적절하지 않은 값이 있습니다.");
        }
    }

    @Override
    public String toString() {
        return "채널이름: "+channelName+"\n관리자UUID: "+adminId;
    }

    //유저ID받아서 채널멤버 목록에 올리기, 목록에서 삭제
    public void addMember(UUID userId){
        if(userId == null){
            throw new UserNotFoundException("유저 ID는 null일 수 없습니다.");
        }
        if(memberId.contains(userId)){
            throw new ChannelOperationException("이미 채널에 참여하고 있는 유저입니다. (ID: " + userId + ")");
        }
        memberId.add(userId);
        updateTime();
    }

    public void removeMember(UUID userId){
        if(userId == null){
            throw new UserNotFoundException("유저 ID는 null일 수 없습니다.");
        }
        if(!memberId.contains(userId)){
            throw new ChannelOperationException("채널에 존재하지 않는 유저입니다. (ID: " + userId + ")");
        }
        memberId.remove(userId);
        updateTime();
    }
}
