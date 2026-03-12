package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;


public class FileChannelService implements ChannelService {
    private final Map<UUID, Channel> data;
    private final String filePath;

    public FileChannelService(String filePath) {
        this.filePath = filePath;
        this.data = loadFromFile();
    }


    @Override
    public Channel createChannel(String name, int capacity, User owner) {
        Channel channel = new Channel(name, capacity, owner);
        data.put(channel.getId(), channel);
        owner.getOwnedChannels().add(channel);
        owner.getJoinedChannels().add(channel);
        saveToFile();
        return channel;
    }

    @Override
    public Channel getChannelById(UUID id) {
        return data.get(id);
    }

    @Override
    public List<Channel> getAllChannels() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void updateChannel(UUID id, String name, int capacity) {
        Channel channel = data.get(id);
        if (channel != null) {
            channel.update(name, capacity);
            saveToFile();
        }
    }

    @Override
    public boolean joinChannel(UUID channelId, User user) {
        Channel channel = data.get(channelId);
        if (channel == null) return false;
        boolean success = channel.addParticipant(user);
        if (success) {
            user.getJoinedChannels().add(channel);
            saveToFile();
        }
        return success;
    }

    @Override
    public boolean leaveChannel(UUID channelId, User user) {
        Channel channel = data.get(channelId);
        if (channel == null) return false;
        boolean success = channel.removeParticipant(user);
        if (success) {
            user.getJoinedChannels().remove(channel);
            saveToFile();
        }
        return success;
    }

    @Override
    public boolean kickUser(UUID channelId, User owner, User targetUser) {
        Channel channel = data.get(channelId);
        if (channel == null || !channel.getOwner().equals(owner)) return false;
        boolean success = channel.removeParticipant(targetUser);
        if (success) {
            targetUser.getJoinedChannels().remove(channel);
            saveToFile();
        }
        return success;
    }

    @Override
    public List<User> getChannelParticipants(UUID channelId) {
        Channel channel = data.get(channelId);
        return channel == null ? null : channel.getParticipants();
    }

    @Override
    public void deleteChannel(UUID id) {
        Channel channel = data.get(id);
        if (channel != null) {
            for (User participant : channel.getParticipants()) {
                participant.getJoinedChannels().remove(channel);
            }
            channel.getOwner().getOwnedChannels().remove(channel);
            data.remove(id);
            saveToFile();
        }
    }


    @SuppressWarnings("unchecked")
    private Map<UUID, Channel> loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[FileChannelService] 파일 로드 실패, 새로 시작합니다: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("[FileChannelService] 파일 저장 실패: " + e.getMessage(), e);
        }
    }
}