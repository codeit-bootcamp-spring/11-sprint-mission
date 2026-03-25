//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.Message;
//import com.sprint.mission.discodeit.service.ChannelService;
//import com.sprint.mission.discodeit.service.MessageService;
//import com.sprint.mission.discodeit.service.UserService;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class FileMessageService implements MessageService {
//
//    private static final String FILE_PATH = "message.ser";
//
//    private final UserService userService;
//    private final ChannelService channelService;
//
//    public FileMessageService(UserService userService, ChannelService channelService) {
//        this.userService = userService;
//        this.channelService = channelService;
//    }
//
//    @SuppressWarnings("unchecked")
//    public List<Message> load() {
//        if (Files.exists(Paths.get(FILE_PATH))) {
//            try (
//                    FileInputStream fis = new FileInputStream(FILE_PATH);
//                    ObjectInputStream ois = new ObjectInputStream(fis);
//            ) {
//                return (List<Message>) ois.readObject();
//            } catch (IOException | ClassNotFoundException e) {
//                throw new RuntimeException("파일에서 메시지 데이터 불러오기 실패", e);
//            }
//        }
//        return new ArrayList<>();
//    }
//
//    public void save(List<Message> data) {
//        try (
//                FileOutputStream fos = new FileOutputStream(FILE_PATH);
//                ObjectOutputStream oos = new ObjectOutputStream(fos);
//        ) {
//            oos.writeObject(data);
//        } catch (IOException e) {
//            throw new RuntimeException("파일 저장 실패", e);
//        }
//    }
//
//
//    @Override
//    public Message create(Message message) {
//        List<Message> data = load();
//        data.add(message);
//        save(data);
//        return message;
//    }
//
//    @Override
//    public List<String> getAllMessage() {
//        List<Message> data = load();
//        return data.stream()
//                .map(this::printMessage)
//                .toList();
//    }
//
//    @Override
//    public List<String> getMessageByChannel(UUID channelId) {
//        List<Message> data = load();
//        return data.stream()
//                .filter(m -> m.getChannelId().equals(channelId))
//                .map(this::printMessage)
//                .toList();
//    }
//
//    @Override
//    public Message findById(UUID id) {
//        List<Message> data = load();
//        return data.stream()
//                .filter(m -> m.getId().equals(id))
//                .findAny()
//                .orElseThrow(() -> new IllegalArgumentException("[message] 없는 id 입니다."));
//    }
//
//    @Override
//    public String getUserName(UUID userId) {
//        return userService.findById(userId).getName();
//    }
//
//    @Override
//    public String getChannelName(UUID channelId) {
//        return channelService.findById(channelId).getName();
//    }
//
//    @Override
//    public String updateContent(UUID id, String content) {
//        List<Message> data = load();
//        Message message = data.stream()
//                .filter(m -> m.getId().equals(id))
//                .findAny()
//                .orElseThrow(() -> new IllegalArgumentException("[message] 없는 id 입니다."));
//        message.updateContent(content);
//        save(data);
//        return printMessage(message);
//    }
//
//    @Override
//    public void delete(UUID id) {
//        List<Message> data = load();
//        Message message = data.stream()
//                .filter(m -> m.getId().equals(id))
//                .findAny()
//                .orElseThrow(() -> new IllegalArgumentException("[message] 없는 id 입니다."));
//        data.remove(message);
//        save(data);
//    }
//
//    @Override
//    public String printMessage(Message message) {
//        return "Message{" +
//                "content='" + message.getContent() + '\'' +
//                ", userName='" + getUserName(message.getUserId()) + '\'' +
//                ", channelId='" + getChannelName(message.getChannelId()) + '\'' +
//                "}";
//    }
//}
