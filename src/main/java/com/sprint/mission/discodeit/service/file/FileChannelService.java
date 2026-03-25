//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.ChannelType;
//import com.sprint.mission.discodeit.service.ChannelService;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//public class FileChannelService implements ChannelService {
//    private static final String FILE_PATH = "channel.ser";
//    private static final Path PATH = Paths.get(FILE_PATH);
//
//    @SuppressWarnings("unchecked")
//    public Map<UUID, Channel> load() {
//        if (Files.exists(PATH)) {
//            try (
//                    FileInputStream fis = new FileInputStream(FILE_PATH);
//                    ObjectInputStream ois = new ObjectInputStream(fis);
//            ) {
//                return (Map<UUID, Channel>) ois.readObject();
//            } catch (IOException | ClassNotFoundException e) {
//                throw new RuntimeException("파일에서 채널 데이터 불러오기 실패", e);
//            }
//        }
//        return new HashMap<>();
//    }
//
//    public void save(Map<UUID, Channel> data) {
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
//    @Override
//    public Channel create(Channel channel) {
//        Map<UUID, Channel> data = load();
//        data.put(channel.getId(), channel);
//        save(data);
//        return data.get(channel.getId());
//    }
//
//    @Override
//    public Map<String, List<Channel>> getAllChannel() {
//        Map<UUID, Channel> data = load();
//        return data.values().stream()
//                .collect(Collectors.groupingBy(Channel::getName));
//    }
//
//    @Override
//    public List<Channel> findByName(String name) {
//        Map<UUID, Channel> data = load();
//        return data.values().stream()
//                .filter(c -> c.getName().equals(name))
//                .toList();
//    }
//
//    @Override
//    public Channel findById(UUID id) {
//        Map<UUID, Channel> data = load();
//        if (data.get(id) == null) {
//            throw new IllegalArgumentException("[channel] 없는 id 입니다.");
//        }
//        return data.get(id);
//    }
//
//    @Override
//    public Channel updateName(UUID id, String name) {
//        Map<UUID, Channel> data = load();
//        Channel channel = data.get(id);
//        if (channel == null) {
//            throw new IllegalArgumentException("[channel] 없는 id 입니다.");
//        }
//        channel.updateName(name);
//        save(data);
//        return channel;
//    }
//
//    @Override
//    public Channel updateDescription(UUID id, String description) {
//        Map<UUID, Channel> data = load();
//        Channel channel = data.get(id);
//        if (channel == null) {
//            throw new IllegalArgumentException("[channel] 없는 id 입니다.");
//        }
//        channel.updateDescription(description);
//        save(data);
//        return channel;
//    }
//
//    @Override
//    public Channel updateType(UUID id, ChannelType type) {
//        Map<UUID, Channel> data = load();
//        if (data.get(id) == null) {
//            throw new IllegalArgumentException("[channel] 없는 id 입니다.");
//        }
//        Channel channel = data.get(id);
//        channel.updateType(type);
//        save(data);
//        return channel;
//    }
//
//    @Override
//    public void delete(UUID id) {
//        Map<UUID, Channel> data = load();
//        if (data.get(id) == null) {
//            throw new IllegalArgumentException("[channel] 없는 id 입니다.");
//        }
//        data.remove(id);
//        save(data);
//    }
//}
