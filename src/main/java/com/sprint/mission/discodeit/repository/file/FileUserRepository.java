package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileUserRepository implements UserRepository {
    private final String USER_FILE = "users.ser";
    private final File file;
    private Map<UUID, User> userMap;

    public FileUserRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        File dir = new File(fileDirectory);

        if(!dir.exists()) {
            dir.mkdirs();
        }
        this.file = new File(dir, USER_FILE);
        this.userMap = loadUsers();
    }

    private Map<UUID, User> loadUsers() {

        if(!file.exists()) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("유저 로드실패", e);
        }
    }

    private void saveUsers() {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(userMap);
        } catch (IOException e) {
            throw new RuntimeException("유저 저장실패", e);
        }
    }

    @Override
    public User save(User user) {
        userMap.put(user.getId() , user);
        saveUsers();
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userMap.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<User> findByUserName(String userName) {
        return userMap.values().stream()
                .filter(user -> user.getUsername().equals(userName))
                .findFirst();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(userMap.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public void delete(UUID id) {
        if(userMap.remove(id) != null) {
            saveUsers();
        } else {
            throw new IllegalArgumentException("존재하지 않는 콘텐츠입니다.");
        }
    }

}
