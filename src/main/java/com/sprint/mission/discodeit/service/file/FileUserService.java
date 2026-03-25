//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.service.UserService;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//public class FileUserService implements UserService {
//
//    private static final String FILE_PATH = "user.ser";
//    private static final Path PATH = Paths.get(FILE_PATH);
//
//    @SuppressWarnings("unchecked")
//    public Map<UUID, User> load() {
//        if (Files.exists(PATH)) {
//            try (
//                    FileInputStream fis = new FileInputStream(FILE_PATH);
//                    ObjectInputStream ois = new ObjectInputStream(fis);
//            ) {
//                return (Map<UUID, User>) ois.readObject();
//
//            } catch (IOException | ClassNotFoundException e) {
//                throw new RuntimeException("파일에서 유저 데이터 불러오기 실패", e);
//            }
//        }
//        else {
//            return new HashMap<>();
//        }
//    }
//
//    public void save(Map<UUID, User> data) {
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
//    public User create(User user) {
//        Map<UUID, User> data = load();
//        data.put(user.getId(), user);
//        save(data);
//        return user;
//    }
//
//    @Override
//    public List<User> getAllUser() {
//        return load().values().stream()
//                .toList();
//    }
//
//    @Override
//    public User findById(UUID id) {
//        Map<UUID, User> data = load();
//        if (data.get(id) == null) {
//            throw new IllegalArgumentException("[user] 없는 id 입니다.");
//        }
//        return data.get(id);
//    }
//
//    @Override
//    public User updateName(UUID id, String name) {
//        Map<UUID, User> data = load();
//        User user = data.get(id);
//        if (user == null) {
//            throw new IllegalArgumentException("[user] 없는 id 입니다.");
//        }
//        user.updateName(name);
//        save(data);
//        return user;
//    }
//
//    @Override
//    public User updateStatus(UUID id, String status) {
//        Map<UUID, User> data = load();
//        User user = data.get(id);
//        if (user == null) {
//            throw new IllegalArgumentException("[user] 없는 id 입니다.");
//        }
//        user.updateStatus(status);
//        save(data);
//        return user;
//    }
//
//    @Override
//    public void delete(UUID id) {
//        Map<UUID, User> data = load();
//        if (data.get(id) == null) {
//            throw new IllegalArgumentException("[user] 없는 id 입니다.");
//        }
//        data.remove(id);
//        save(data);
//    }
//}
