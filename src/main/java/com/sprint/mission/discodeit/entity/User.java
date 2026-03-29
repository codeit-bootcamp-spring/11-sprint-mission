package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelOperationException;
import com.sprint.mission.discodeit.exception.user.InvalidUserRequestException;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
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

    //getter
    public List<UUID> getJoinedChannelId() {
        return Collections.unmodifiableList(this.joinedChannelId);
    }

    //업데이트 메소드
    public void update(String userName, String email, String password) {
        if (StringUtils.hasText(userName) && StringUtils.hasText(email) && StringUtils.hasText(password)){
            this.userName = userName;
            this.email = email;
            this.password = password;

            updateTime();
        } else {
            throw new InvalidUserRequestException("유저 정보를 갱신하는데 적절하지 않은 값이 있습니다.");
        }
    }

    @Override
    public String toString() {
        return "닉네임: "+userName+"\n이메일: "+email+"\n비밀번호: "+password;
    }

    //채널 ID받아서 속한 채널 리스트에 올리기, 리스트에서 삭제하기
    public void joinChannel(UUID channelId){
        if(channelId == null) {
            throw new ChannelNotFoundException("채널 ID는 null일 수 없습니다.");
        }
        if(joinedChannelId.contains(channelId)){
            throw new ChannelOperationException("이미 채널에 속해있습니다.");
        }
        this.joinedChannelId.add(channelId);
        updateTime();
    }

    public void leaveChannel(UUID channelId){
        if(channelId == null){
            throw new ChannelNotFoundException("채널 ID는 null일 수 없습니다.");
        }
        if(!joinedChannelId.contains(channelId)){
            throw new ChannelOperationException("이 채널에 속해있지 않습니다.");
        }
        this.joinedChannelId.remove(channelId);
        updateTime();
    }

    public void updateProfileId(UUID profileId) {
        this.profileId = profileId;
        updateTime();
    }
}
