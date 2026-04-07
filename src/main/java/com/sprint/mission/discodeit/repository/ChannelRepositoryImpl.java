package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChannelRepositoryImpl implements ChannelRepository {

  private final Map<UUID, Channel> store = new HashMap<>();
  private final ReadStatusRepository readStatusRepository;  // final 추가!

  @Override
  public Channel save(Channel channel) {
    store.put(channel.getId(), channel);
    return channel;
  }

  @Override
  public Optional<Channel> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Optional<Channel> findByName(String name) {
    return store.values().stream()
        .filter(ch -> name != null && name.equals(ch.getName()))
        .findFirst();
  }

  @Override
  public List<Channel> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<Channel> findAllByIsPrivate(boolean isPrivate) {
    return store.values().stream()
        .filter(ch -> ch.isPrivate() == isPrivate)
        .toList();
  }

  @Override
  public List<Channel> findAllPrivateByUserId(UUID userId) {
    List<UUID> channelIds = readStatusRepository.findAllByUserId(userId)
        .stream()
        .map(ReadStatus::getChannelId)  // 메서드 참조로 변경
        .toList();

    return store.values().stream()
        .filter(ch -> ch.isPrivate() && channelIds.contains(ch.getId()))
        .toList();
  }

  @Override
  public void delete(UUID id) {
    store.remove(id);
  }

  @Override
  public void deleteById(UUID id) {
    store.remove(id);
  }
}