package com.sprint.mission.discodeit.repository.interaction;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BasicUserStatusRepository implements com.sprint.mission.discodeit.repository.UserStatusRepository {
    private final String STATUS_FILE = "userStatus.ser";
    private final Map<UUID, UserStatus> userStatusMap;

    public BasicUserStatusRepository() {
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

    public void save(UserStatus status) {
        userStatusMap.put(status.getUserId(), status);
    }

    public Optional<UserStatus> findById(UUID id) {
        return userStatusMap.values().stream()
                .filter(userStatus -> userStatus.getUserId().equals(id))
                .findFirst();
    }

    public void deleteByUserId(UUID userId) {
        userStatusMap.entrySet()
                .removeIf(entry -> entry.getValue().getUserId().equals(userId));
    }

}
