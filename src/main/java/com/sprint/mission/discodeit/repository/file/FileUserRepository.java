package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.util.*;

public class FileUserRepository implements UserRepository {
    private final File file;
    private Map<UUID, User> data;

    public FileUserRepository() {
        this("data/users.ser");
    }

    public FileUserRepository(String filePath) {
        this.file = new File(filePath);
        this.data = load();
    }

    @Override
    public synchronized User save(User user) {
        data.put(user.getId(), user);
        persist();
        return user;
    }

    @Override
    public synchronized User findById(UUID id) {
        return data.get(id);
    }

    @Override
    public User findByEmail(String email) {
        return data.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public User findByUserName(String userName) {
        return data.values().stream()
                .filter(user -> user.getUserName().equals(userName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public synchronized List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public synchronized void delete(UUID id) {
        data.remove(id);
        persist();
    }

    // ---------- persistence ----------
    @SuppressWarnings("unchecked")
    private Map<UUID, User> load() {
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof Map<?, ?>) {
                return (Map<UUID, User>) obj;
            }
            return new HashMap<>();
        } catch (EOFException e) {
            return new HashMap<>();
        } catch (Exception e) {
            throw new IllegalStateException("유저 데이터 로드 실패 : " + file.getAbsolutePath(), e);
        }
    }

    private void persist() {
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new IllegalStateException("유저 데이터 저장 실패 : " + file.getAbsolutePath(), e);
        }
    }
}
