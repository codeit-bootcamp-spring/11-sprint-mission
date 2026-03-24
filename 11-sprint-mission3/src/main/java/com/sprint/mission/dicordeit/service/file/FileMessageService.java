package com.sprint.mission.dicordeit.service.file;

import com.sprint.mission.dicordeit.entity.Message;
import com.sprint.mission.dicordeit.service.MessageService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMessageService implements MessageService {

    private static final String FILE_PATH = "messages.dat";

    private List<Message> loadMessages() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(file))) {

            return (List<Message>) ois.readObject();

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveMessages(List<Message> messages) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {

            oos.writeObject(messages);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Message sendMessage(String content, UUID senderId, UUID channelId) {
        List<Message> messages = loadMessages();

        Message message = new Message(content, senderId, channelId);
        messages.add(message);

        saveMessages(messages);
        return message;
    }

    @Override
    public Message correction(UUID messageId, String newContent) {
        List<Message> messages = loadMessages();

        for (Message message : messages) {
            if (message.getId().equals(messageId)) {
                message.update(newContent);
                saveMessages(messages);
                return message;
            }
        }
        return null;
    }

    @Override
    public void delete(UUID messageId) {
        List<Message> messages = loadMessages();
        messages.removeIf(message -> message.getId().equals(messageId));
        saveMessages(messages);
    }

    @Override
    public Message readMessage(UUID messageId) {
        return loadMessages().stream()
                .filter(message -> message.getId().equals(messageId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Message> readallMessage() {
        return loadMessages();
    }
}