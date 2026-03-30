package com.sprint.mission.dicordeit.service.file;

import com.sprint.mission.dicordeit.entity.User;
import com.sprint.mission.dicordeit.service.UserService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUserService implements UserService {

    private static final String FILE_PATH = "users.dat";

    private List<User> loadUsers() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(file))) {

            return (List<User>) ois.readObject();

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveUsers(List<User> users) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {

            oos.writeObject(users);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User createUser(String username, String password, String email) {
        List<User> users = loadUsers();

        User newUser = new User(username, password, email);
        users.add(newUser);

        saveUsers(users);
        return newUser;
    }

    @Override
    public User readUser(UUID id) {
        return loadUsers().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> readAll() {
        return loadUsers();
    }

    @Override
    public User update(UUID id, String password, String email) {
        List<User> users = loadUsers();

        for (User user : users) {
            if (user.getId().equals(id)) {
                user.update(password, email);
                saveUsers(users);
                return user;
            }
        }
        return null;
    }

    @Override
    public void delete(UUID id) {
        List<User> users = loadUsers();
        users.removeIf(user -> user.getId().equals(id));
        saveUsers(users);
    }
}