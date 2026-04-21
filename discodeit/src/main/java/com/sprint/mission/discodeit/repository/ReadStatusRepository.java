package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

    // user, profile, status를 즉시 로딩 (ChannelMapper에서 participants 조회 시 N+1 방지)
    @EntityGraph(attributePaths = {"user", "user.profile", "user.status"})
    List<ReadStatus> findAllByChannelId(UUID channelId);

    // channel을 즉시 로딩 (ChannelService에서 visiblePrivateChannelIds 조회 시 N+1 방지)
    @EntityGraph(attributePaths = {"channel"})
    List<ReadStatus> findAllByUserId(UUID userId);

    boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);

    void deleteAllByChannelId(UUID channelId);
}
