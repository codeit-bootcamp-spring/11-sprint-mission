package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
public class FileUserStatusRepository implements UserStatusRepository {
    private final String STATUS_FILE = "userStatus.ser";
    private final Map<UUID, UserStatus> userStatusMap;

    public FileUserStatusRepository() {
        this.userStatusMap = loadStatus();
    }

    private Map<UUID, UserStatus> loadStatus() {
        File file = new File(STATUS_FILE);

        if(!file.exists()) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, UserStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("콘텐츠 로드실패", e);
        }
    }

    private void saveContents() {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STATUS_FILE))) {
            oos.writeObject(userStatusMap);
        } catch (IOException e) {
            throw new RuntimeException("유저 저장실패", e);
        }
    }

    @Override
    public void save(UserStatus status) {
        userStatusMap.put(status.getId(), status);
        saveContents();
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return Optional.ofNullable(userStatusMap.get(id));
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return userStatusMap.values().stream()
                .filter(status -> status.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        return new ArrayList<>(userStatusMap.values());
    }

    @Override
    public void deleteByUserId(UUID userId) {
        userStatusMap.entrySet()
                .removeIf(entry -> entry.getValue().getUserId().equals(userId));
        saveContents();
    }

}
