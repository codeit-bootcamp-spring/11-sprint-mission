package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JPAChannelRepository extends JpaRepository<Channel, UUID> {

  @Query("SELECT c FROM Channel c JOIN ReadStatus rs ON rs.channel.id = c.id WHERE rs.user.id = :userId")
  List<Channel> findAllByUser_Id(UUID userId);

  @Query("select c.type from Channel c where c.id = :channelId")
  ChannelType findTypeById(UUID channelId);


}
