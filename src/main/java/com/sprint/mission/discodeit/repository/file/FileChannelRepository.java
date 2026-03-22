package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.util.FileIOUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileChannelRepository implements ChannelRepository {
    private final FileIOUtil<Channel> fileIOUtil;

    public FileChannelRepository() {
        this.fileIOUtil = new FileIOUtil<>(Channel.class);
    }

    @Override
    public void save(Channel channel) {
        this.fileIOUtil.save(channel);
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(this.fileIOUtil.findById(id));
    }

    @Override
    public List<Channel> findAll() {
        return this.fileIOUtil.findAll();
    }

    @Override
    public boolean existByName(String name) {
        return this.fileIOUtil.findAll().stream()
                .anyMatch(channel -> channel.getName().equals(name));
    }

    @Override
    public void delete(Channel channel) {
        this.fileIOUtil.delete(channel);
    }
}
