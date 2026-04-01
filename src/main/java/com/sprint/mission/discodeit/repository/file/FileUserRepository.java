package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(prefix = "discodeit.repository", name = "type", havingValue = "file")
public class FileUserRepository implements UserRepository {
    private void saveUsers(List<User> users) {
        try (FileOutputStream fos = new FileOutputStream("user.ser");
             ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<User> loadUsers() {
        File file = new File("user.ser");

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileInputStream fis = new FileInputStream("user.ser");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(User user) {
        List<User> users = loadUsers();
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);
        saveUsers(users);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return loadUsers().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return loadUsers();
    }

    @Override
    public void delete(UUID id) {
        List<User> users = loadUsers();
        users.removeIf(user -> user.getId().equals(id));
        saveUsers(users);
    }

    @Override
    public boolean existsByName(String name) {
        return loadUsers().stream().anyMatch(user ->
                user.getName().equals(name));
    }

    @Override
    public boolean existsByEmail(String email) {
        return loadUsers().stream().anyMatch(user ->
                user.getEmail().equals(email));
    }

    @Override
    public Optional<User> findByName(String name) {
        return loadUsers().stream().filter(user ->
                user.getName().equals(name)).findFirst();
    }
}