package com.sprint.mission.discodeit.entity.Domain;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class Channel extends BaseEntity implements Serializable {
    private String channelName;     // 채널이름(채널명)
    private String channelDescription;      // 채널주소
    private static final long serialVersionUID = 1L;

    public Channel(String channelName, String channelDescription){  // 채널을 새로만듬(이름, 주소)
        super();    // 유효아이디, 만든시간 업데이트
        this.channelName = channelName;
        this.channelDescription = channelDescription;
    }

    public void updateChannel(String channelName, String channelDescription) {
        this.channelName = channelName;     // 채널 업데이트하는 설정
        this.channelDescription = channelDescription;
        updateTimestamp();  // 채널 이름 업데이트 시간 넣어주기
    }

    @Override
    public String toString() {
        return
                "Channel ---- " + "[id=" + id + "] " +
                        "[channelName='" + channelName + "'] " +
                        "[channelDescription='" + channelDescription + "']" +
                        " [createdAt='" + createdAt + "']" +
                        " [updatedAt='" + updatedAt + "']";
    }
}
