package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.util.*;

public class FileMessageService {

    private final File file;
    private final UserService userService;
    private final ChannelService channelService;

    public FileMessageService(UserService userService, ChannelService channelService) {
        this.file = new File("messages.ser");
        this.userService = userService;
        this.channelService = channelService;
    }


    public Message create(UUID userId, UUID channelId, String content) {
        if (userService.find(userId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        if (channelService.find(channelId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        Map<UUID, Message> data = load();
        Message message = new Message(userId, channelId, content);
        data.put(message.getId(), message);
        save(data);
        return message;
    }

    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(load().get(id));
    }


    public List<Message> findAll() {
        return new ArrayList<>(load().values());
    }


    public Message update(UUID id, String content) {
        Map<UUID, Message> data = load();
        Message message = data.get(id);

        if (message == null) {
            return null;
        }

        message.update(content);
        save(data);
        return message;
    }


    public void delete(UUID id) {
        Map<UUID, Message> data = load();
        data.remove(id);
        save(data);
    }

    private void save(Map<UUID, Message> data) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, Message> load() {
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Message>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}