package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository; // 추가
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {
  // 기본 CRUD 메서드는 모두 자동 생성
}