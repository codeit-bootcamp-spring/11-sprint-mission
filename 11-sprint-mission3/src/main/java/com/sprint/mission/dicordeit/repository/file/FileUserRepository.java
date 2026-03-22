package com.sprint.mission.dicordeit.repository.file;

import com.sprint.mission.dicordeit.entity.User;
import com.sprint.mission.dicordeit.repository.UserRepository;

import java.io.*;
import java.util.*;

public class FileUserRepository implements UserRepository {

    private static final String FILE_PATH = "users.dat";

    private Map<UUID, User> load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new HashMap<>();

        try (ObjectInputStream ois =
                new ObjectInputStream(new FileInputStream(file))) {

            return (Map<UUID, User>) ois.readObject();

        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void saveAll(Map<UUID, User> data) {
        try (ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {

            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(User user) {
        Map<UUID, User> data = load();
        data.put(user.getId(), user);
        saveAll(data);
    }

    @Override
    public User findById(UUID id) {
        return load().get(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(load().values());
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, User> data = load();
        data.remove(id);
        saveAll(data);
    }
}
