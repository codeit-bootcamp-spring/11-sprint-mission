package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.Optional;
import java.io.*;
import java.util.*;

public class FileUserService {

    private final File file;

    public FileUserService() {
        this.file = new File("users.ser");
    }

    public User create(String username, String email, String password) {
        Map<UUID, User> data = load();
        User user = new User(username, email, password);
        data.put(user.getId(), user);
        save(data);
        return user;
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(load().get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(load().values());
    }

    public User update(UUID id, String username, String email, String password) {
        Map<UUID, User> data = load();
        User user = data.get(id);

        if (user == null) {
            return null;
        }

        user.update(username, email, password);
        save(data);
        return user;
    }

    public void delete(UUID id) {
        Map<UUID, User> data = load();
        data.remove(id);
        save(data);
    }

    private void save(Map<UUID, User> data) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<UUID, User> load() {
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, User>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}