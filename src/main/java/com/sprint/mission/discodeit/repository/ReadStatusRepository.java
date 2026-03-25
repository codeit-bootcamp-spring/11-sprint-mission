package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository {
    void insert(ReadStatus readStatus);
    ReadStatus findById(UUID id); // 특정 유저가 속한 특정 채널에서 마지막 읽은 시간 기록 조회
    List<ReadStatus> findByUserId(UUID userId); // 특정 유저가 속한 모든 채널의 마지막으로 메시지를 읽은 시간 기록 조회
    List<ReadStatus> findByChannelId(UUID channelId); // 특정 채널에 속한 모든 유저의 마지막으로 메시지를 읽은 시간 기록 조회
    void update(ReadStatus readStatus);
    void delete(UUID id);
    void deleteAllByChannelId(UUID channelId); // 특정 채널 삭제시 해당 채널에 속한 모든 유저의 마지막으로 메시지를 읽은 시간 기록 삭제
}
