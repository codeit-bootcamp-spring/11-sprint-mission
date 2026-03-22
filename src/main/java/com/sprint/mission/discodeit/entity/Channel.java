package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.util.StringUtil;
import lombok.Getter;

import java.io.Serial;
import java.util.ArrayList;
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

    //업데이트 메소드
    public void update(String channelName, UUID adminId) {
        if(StringUtil.isValid(channelName) && adminId != null){ //유효한지 검사
            this.channelName = channelName;
            this.adminId = adminId;
            if(!memberId.contains(adminId)){
                memberId.add(adminId);
            }
            updateTime(); // 업데이트 시간 갱신
        } else {
            System.out.println("채널 정보를 갱신에 적절하지 않은 값이 있습니다.");
        }
    }

    @Override
    public String toString() {
        return "채널이름: "+channelName+"\n관리자UUID: "+adminId;
    }

    //유저ID받아서 채널멤버 목록에 올리기, 목록에서 삭제
    public void addMember(UUID userId){
        if(userId == null){
            System.out.println("존재하지 않는 유저입니다.");
            return;
        }
        if(memberId.contains(userId)){
            System.out.println("이미 채널 멤버입니다.");
            return;
        }
        memberId.add(userId);
        updateTime();
    }

    public void removeMember(UUID userId){
        if(userId == null){
            System.out.println("존재하지 않는 유저입니다.");
            return;
        }
        if(!memberId.contains(userId)){
            System.out.println("채널 멤버가 아닙니다.");
            return;
        }
        memberId.remove(userId);
        updateTime();
    }
}