package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Domain.ReadStatus;
import com.sprint.mission.discodeit.entity.Domain.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository {
    // 사용자가 채널별 마지막으로 메세지를 읽은 시간을 표현

    // 상태 생성 > user가 처음 채널에 입장
    ReadStatus create(ReadStatus readStatus);

    // user id + channel id 로 특정 유저의 특정 채널 readstatus조회하기
    ReadStatus readByUserIdAndChannelId(UUID userId, UUID channelId);

    // channel id로 해당 채널의 모든 유저의 readstatus를 조회하기
    List<ReadStatus> readAllByChannelId(UUID channelId);

    // user id , channel id 로 마지막에 읽은 시간을 업데이트 하기(유저가 메세지 읽을때)
    ReadStatus update(UUID userId, UUID channelId, Instant lastMessageReadAt);

    // channel id 기반으로 삭제 > 채널이 삭제되면 읽은 기록을 지워야함.
    void deleteByChannelId(UUID channelId);

    // user id 기반으로 삭제 > 유저가 삭제되면 채널에 안남으니까 기록 지워야함.
    void deleteByUserId(UUID userId);
}

