package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
// application.yaml의 설정값에 따라 Bean을 설정 / name : 설정값의 이름, havingValue : type 지정
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileUserStatusRepository implements UserStatusRepository {
    private final Map<UUID, UserStatus> userStatuses = new HashMap<>();

    private final String fileDirectory;
    private final String fileName = "/userStatuses.ser";
    private final File file;

    public FileUserStatusRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        this.fileDirectory = fileDirectory;
        this.file = new File(fileDirectory + fileName);
        load();
    }

    // 저장 메서드 save(직렬화)
    private void save() {
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(userStatuses);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 불러오기 메서드 load(역직렬화)
    private void load() {
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Map<UUID, UserStatus> loadUserStatuses = (Map<UUID, UserStatus>) ois.readObject();
            userStatuses.clear(); // 한 번 비우고
            userStatuses.putAll(loadUserStatuses); // 불러온다.(기존에 있던 데이터까지 같이 로드될 수 있기 때문에)
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void insert(UserStatus userStatus) {
        userStatuses.put(userStatus.getId(), userStatus);
        save();
    }

    @Override
    public UserStatus findById(UUID id) {
        UserStatus userStatus = userStatuses.get(id);
        if (userStatus == null) {
            throw new NoSuchElementException("해당 UserStatus가 존재하지 않습니다 id : " + id);
        }

        return userStatus;
    }

    @Override
    public UserStatus findByUserId(UUID userId) {
        return userStatuses.values().stream()
                .filter(userStatus -> userStatus.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("해당 UserStatus가 존재하지 않습니다 id : " + userId));
    }

    @Override
    public List<UserStatus> findAll() {
        return userStatuses.values().stream().toList();
    }

    @Override
    public void update(UserStatus userStatus) {
        userStatuses.put(userStatus.getId(), userStatus);
        save();
    }

    @Override
    public void delete(UUID id) {
        userStatuses.remove(id);
        save();
    }

    @Override
    public void deleteByUserId(UUID userId) {
        userStatuses.values().removeIf(status -> status.getUserId().equals(userId));
        save();
    }
}
