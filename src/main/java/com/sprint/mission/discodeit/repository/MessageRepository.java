package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;



public interface MessageRepository extends JpaRepository<Message, UUID> {
    Slice<Message> findAllByChannel_Id(UUID channelId, Pageable pageable);
    List<Message> findAllByChannel_Id(UUID channelId);
}