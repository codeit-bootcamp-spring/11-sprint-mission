package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class FileChannelService extends FileUtil implements ChannelService {

    public FileChannelService() {
        super("channels");
    }

    @Override
    public Channel create(ChannelType channelType, String name, String description) {
        Channel channel = new Channel(channelType, name, description);
        save(filePath(channel.getId()), channel);
        return channel;
    }

    @Override
    public Channel findById(UUID id) {
        Path path = filePath(id);
        if(!Files.exists(path)) {
            throw new IllegalArgumentException("Channel Not Found");
        }
        return load(path, Channel.class);
    }

    @Override
    public List<Channel> findAll() {
        return loadAll(directory, Channel.class);
    }

    @Override
    public void update(Channel oldChannel, Channel newChannel) {
        // UUID를 유지하기 위해 remove -> add 하지 않음
        oldChannel.setChannelType(newChannel.getChannelType());
        oldChannel.setName(newChannel.getName());
        oldChannel.setDescription(newChannel.getDescription());
        oldChannel.update();
        save(filePath(oldChannel.getId()), oldChannel);
    }

    @Override
    public void delete(Channel channel) {
        delete(filePath(channel.getId()));
    }

}
