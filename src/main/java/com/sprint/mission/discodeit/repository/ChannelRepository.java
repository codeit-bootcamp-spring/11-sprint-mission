package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Domain.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelRepository {
    // 생성, 단건 조회, 전체 조회, 수정, 삭제
    UUID create(Channel channel);

    // 단건 조회
    Channel read(UUID id);

    // 전체 조회
    List<Channel> readAll();

    // 삭제
    void delete(UUID id);

    // 복구
    void restore(UUID id);
}
