package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;

import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JPAMessageRepository extends JpaRepository<Message, UUID> {


  @EntityGraph(attributePaths = {"author", "channel", "author.status", "author.profile"})
  @Query("SELECT m FROM Message m WHERE :cursor IS NULL OR m.createdAt < :cursor")
  Slice<Message> findAllByChannel_Id(UUID channelId, Pageable pageable, Instant cursor);

  @EntityGraph(attributePaths = {"author", "channel", "author.status", "author.profile"})
  Slice<Message> findAllByChannel_Id(UUID channelId, Pageable pageable);

  List<Message> findAllByAuthor(User author);

  Optional<Message> findTopByChannel_IdOrderByCreatedAtDesc(UUID channelId);


  void deleteById(UUID id);
}
