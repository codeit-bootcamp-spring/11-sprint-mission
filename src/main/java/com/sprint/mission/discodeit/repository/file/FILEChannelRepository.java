package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FILEChannelRepository implements ChannelRepository {

    private final FileSaveLoad<Channel> saveLoad;
    private final Path directory;

    public FILEChannelRepository(@Value("${discodeit.repository.file-dir}") String path) {

        saveLoad = new FileSaveLoad<>();
        directory = Path.of(path + "/Channels/");
    }

    @Override
    public boolean saveChannel(Channel channel) {
        if(channel == null)
            return false;

        save(idToPath(channel.getId()),channel);
        return true;
    }

    @Override
    public Optional<Channel> getChannel(UUID channelId) {
        Map<UUID,Channel> channels = load(directory);
        return Optional.ofNullable(channels.get(channelId));

    }

    @Override
    public List<Channel> getAllChannel() {

        Map<UUID,Channel> channels = load(directory);
        return channels.values().stream().toList();
    }

    @Override
    public boolean deleteChannel(UUID channelId) {

        if(!isExistChannel(channelId)){
            return false;
        }
        try {
            Files.deleteIfExists(idToPath(channelId));
            return true;
        }
        catch(IOException e){
            return false;
        }

    }

    @Override
    public boolean isExistChannel(UUID channelId) {
        Map<UUID,Channel> channels = load(directory);
        return channels.containsKey(channelId);


    }

    private Map<UUID,Channel> load(Path directory) {
        return saveLoad.load(directory);
    }

    private void save(Path filePath, Channel channel) {

        saveLoad.save(filePath, channel);

    }
    private Path idToPath(UUID channelId){

        return directory.resolve(channelId + ".dat");

    }











}
