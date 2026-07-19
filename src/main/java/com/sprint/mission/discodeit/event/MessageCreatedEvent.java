package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.data.MessageDto;

/**
 * 채널에 새로운 메시지가 등록되었음을 의미하는 이벤트입니다.
 * 알림 생성 리스너가 이 이벤트를 받아 해당 채널을 구독 중인(알림이 켜진) 사용자들에게
 * 알림을 생성합니다.
 */
public record MessageCreatedEvent(
    MessageDto message,
    String channelName
) {

}
