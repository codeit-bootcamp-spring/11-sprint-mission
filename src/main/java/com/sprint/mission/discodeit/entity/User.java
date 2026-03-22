package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.util.StringUtil;
import lombok.Getter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter // getter 메소드를 @Getter로 대체
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userName;
    private String email;
    private String password;
    private final List<UUID> joinedChannelId = new ArrayList<>();

    private UUID profileId;// 프로필 사진id

    //생성자
    public User(String userName, String email, String password){
        super();
        this.userName = userName;
        this.password = password;
        this.email = email;
    }

    //업데이트 메소드
    public void update(String userName, String email, String password) {
        if(StringUtil.isValid(userName) && StringUtil.isValid(email) && StringUtil.isValid(password)){
            this.userName = userName;
            this.email = email;
            this.password = password;

            updateTime();
        } else {
            System.out.println("유저 정보를 갱신에 적절하지 않은 값이 있습니다.");
        }
    }

    @Override
    public String toString() {
        return "닉네임: "+userName+"\n이메일: "+email+"\n비밀번호: "+password;
    }

    //채널 ID받아서 속한 채널 리스트에 올리기, 리스트에서 삭제하기
    public void joinChannel(UUID channelId){
        if(channelId == null) {
            System.out.println("채널 id가 null입니다.");
            return;
        }
        if(joinedChannelId.contains(channelId)){
            System.out.println("이미 채널에 속해있습니다.");
            return;
        }
        this.joinedChannelId.add(channelId);
        updateTime();
    }

    public void leaveChannel(UUID channelId){
        if(channelId == null){
            System.out.println("채널 id가 null입니다.");
            return;
        }
        if(!joinedChannelId.contains(channelId)){
            System.out.println("이 채널에 속해있지 않습니다.");
            return;
        }
        this.joinedChannelId.remove(channelId);
        updateTime();
    }

    public void updateProfileId(UUID profileId) {
        this.profileId = profileId;
        updateTime();
    }
}