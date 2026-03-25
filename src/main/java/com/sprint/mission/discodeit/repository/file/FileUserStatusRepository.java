package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(prefix = "discodeit.repository", name = "type", havingValue = "file")
public class FileUserStatusRepository implements UserStatusRepository {
    private void saveAll(List<UserStatus> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("user_status.ser"))) {
            oos.writeObject(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<UserStatus> loadAll() {
        File file = new File("user_status.ser");
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))){
            return (List<UserStatus>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public UserStatus save(UserStatus userStatus) {
        List<UserStatus> list = loadAll();
        list.removeIf(us -> us.getId().equals(userStatus.getId()));
        list.add(userStatus);
        saveAll(list);
        return userStatus;
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return loadAll().stream().filter(us ->
                us.getUserId().equals(userId)).findFirst();
    }

    @Override
    public void deleteByUserId(UUID userId) {
        List<UserStatus> list = loadAll();
        list.removeIf(us -> us.getUserId().equals(userId));
        saveAll(list);
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return loadAll().stream().filter(us ->
                us.getId().equals(id)).findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        return loadAll();
    }

    @Override
    public void delete(UUID id) {
        List<UserStatus> list = loadAll();
        list.removeIf(us -> us.getId().equals(id));
        saveAll(list);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return loadAll().stream().anyMatch(us ->
                us.getUserId().equals(userId));
    }
}
