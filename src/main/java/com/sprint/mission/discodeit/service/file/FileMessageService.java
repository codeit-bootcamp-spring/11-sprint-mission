//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.Message;
//import com.sprint.mission.discodeit.service.MessageService;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class FileMessageService implements MessageService {
//    public static void saveMessages(List<Message> messages) {
//        try (FileOutputStream fos = new FileOutputStream("message.ser");
//             ObjectOutputStream oos = new ObjectOutputStream(fos);
//        ) {
//            oos.writeObject(messages);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private List<Message> loadMessages() {
//        File file = new File("message.ser");
//
//        if (!file.exists()) {
//            return new ArrayList<>();
//        }
//
//        try (FileInputStream fis = new FileInputStream("message.ser");
//             ObjectInputStream ois = new ObjectInputStream(fis)) {
//            return (List<Message>) ois.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
//
//
//    @Override
//    public Message create(String name,UUID userId, UUID channelId) {
//        List<Message> messages = loadMessages();
//        Message message = new Message(name, userId, channelId);
//        messages.add(message);
//        saveMessages(messages);
//        return message;
//    }
//
//    @Override
//    public Message findById(UUID id) {
//        return loadMessages().stream()
//                .filter(message -> message.getId().equals(id))
//                .findFirst().orElse(null);
//    }
//
//    @Override
//    public List<Message> findAll() {
//        return loadMessages();
//    }
//
//    @Override
//    public void update(UUID id, String name) {
//        List<Message> messages = loadMessages();
//        boolean isUpdated = false;
//        for (Message message : messages) {
//            if (message.getId().equals(id)) {
//                message.update(name);
//                isUpdated = true;
//                break;
//            }
//        }
//        if (isUpdated) {
//            saveMessages(messages);
//        }
//    }
//
//    @Override
//    public void delete(UUID id) {
//        List<Message> messages = loadMessages();
//        messages.removeIf(message -> message.getId().equals(id));
//        saveMessages(messages);
//    }
//}
