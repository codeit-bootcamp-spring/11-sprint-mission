package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;


public class FileUserService implements UserService {
    private final Map<UUID, User> data;
    private final String filePath;

    public FileUserService(String filePath) {
        this.filePath = filePath;
        this.data = loadFromFile();
    }


    @Override
    public User createUser(String name, String email) {
        User user = new User(name, email);
        data.put(user.getId(), user);
        saveToFile();
        return user;
    }

    @Override
    public User getUserById(UUID id) {
        return data.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void updateUser(UUID id, String name, String email) {
        User user = data.get(id);
        if (user != null) {
            user.update(name, email);
            saveToFile();
        }
    }

    @Override
    public void deleteUser(UUID id) {
        data.remove(id);
        saveToFile();
    }


    @SuppressWarnings("unchecked")
    private Map<UUID, User> loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[FileUserService] 파일 로드 실패, 새로 시작합니다: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("[FileUserService] 파일 저장 실패: " + e.getMessage(), e);
        }
    }
}