package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.MessageEditHistory;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.MessageService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class FileMessageService implements MessageService {
    private final Map<UUID, Message> data;
    private final String filePath;

    public FileMessageService(String filePath) {
        this.filePath = filePath;
        this.data = loadFromFile();
    }


    @Override
    public Message sendDirectMessage(User sender, User receiver, String content) {
        Message message = new Message(sender, receiver, content);
        data.put(message.getId(), message);
        sender.getSentMessages().add(message);
        receiver.getReceivedMessages().add(message);
        saveToFile();
        return message;
    }

    @Override
    public Message sendChannelMessage(User sender, Channel channel, String content) {
        Message message = new Message(sender, channel, content);
        data.put(message.getId(), message);
        sender.getSentMessages().add(message);
        channel.addMessage(message);
        saveToFile();
        return message;
    }

    @Override
    public Message getMessageById(UUID id) {
        return data.get(id);
    }

    @Override
    public List<Message> getAllMessages() {
        return new ArrayList<>(data.values());
    }

    @Override
    public List<Message> getMessagesBySender(User sender) {
        return new ArrayList<>(sender.getSentMessages());
    }

    @Override
    public List<Message> getMessagesInChannel(Channel channel) {
        return new ArrayList<>(channel.getMessages());
    }

    @Override
    public void updateMessage(UUID messageId, String newContent) {
        Message message = data.get(messageId);
        if (message != null && !message.isDeleted()) {
            message.update(newContent);
            saveToFile();
        }
    }

    @Override
    public void deleteMessage(UUID messageId) {
        Message message = data.get(messageId);
        if (message != null) {
            message.delete();
            saveToFile();
        }
    }

    @Override
    public List<MessageEditHistory> getMessageEditHistory(UUID messageId) {
        Message message = data.get(messageId);
        if (message == null) return new ArrayList<>();
        return new ArrayList<>(message.getEditHistories());
    }

    @Override
    public boolean isMessageDeleted(UUID messageId) {
        Message message = data.get(messageId);
        return message != null && message.isDeleted();
    }


    @SuppressWarnings("unchecked")
    private Map<UUID, Message> loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[FileMessageService] 파일 로드 실패, 새로 시작합니다: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("[FileMessageService] 파일 저장 실패: " + e.getMessage(), e);
        }
    }
}