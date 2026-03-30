package com.sprint.mission.dicordeit.service.file;

import com.sprint.mission.dicordeit.entity.Channel;
import com.sprint.mission.dicordeit.service.ChannelService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileChannelService implements ChannelService {

    private static final String FILE_PATH = "channels.dat";

    private List<Channel> loadChannels() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(file))) {

            return (List<Channel>) ois.readObject();

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveChannels(List<Channel> channels) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {

            oos.writeObject(channels);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Channel createChannel(String name, String description) {
        List<Channel> channels = loadChannels();

        Channel newChannel = new Channel(name, description);
        channels.add(newChannel);

        saveChannels(channels);
        return newChannel;
    }

    @Override
    public Channel readChannel(UUID channelId) {
        return loadChannels().stream()
                .filter(channel -> channel.getId().equals(channelId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Channel> allChannel() {
        return loadChannels();
    }

    @Override
    public Channel updateChannel(UUID channelId, String newName, String newDescription) {
        List<Channel> channels = loadChannels();

        for (Channel channel : channels) {
            if (channel.getId().equals(channelId)) {
                channel.update(newName, newDescription);
                saveChannels(channels);
                return channel;
            }
        }
        return null;
    }

    @Override
    public void deleteChannel(UUID channelId) {
        List<Channel> channels = loadChannels();
        channels.removeIf(channel -> channel.getId().equals(channelId));
        saveChannels(channels);
    }
}