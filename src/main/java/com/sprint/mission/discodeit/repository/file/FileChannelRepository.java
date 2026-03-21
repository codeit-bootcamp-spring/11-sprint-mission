package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class FileChannelRepository extends FileRepository<Channel> implements ChannelRepository {
    public FileChannelRepository() {
        super(Channel.class);
    }

    @Override
    public void save(Channel channel) {
        super.save(channel);
    }

    @Override
    public Channel findById(UUID id) {
        Channel channel = super.findById(id);
        if (channel == null) throw new IllegalArgumentException("requested channel not found. ❌");

        return channel;
    }

    @Override
    public boolean existByName(String name) {
        return super.findAll().stream()
                .anyMatch(channel -> channel.getName().equals(name));
    }

    @Override
    public List<Channel> findAll() {
        return super.findAll();
    }

    @Override
    public void delete(Channel channel) {
        super.delete(channel);
    }
}
