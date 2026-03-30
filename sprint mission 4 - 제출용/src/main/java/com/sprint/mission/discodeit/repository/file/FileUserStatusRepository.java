package com.sprint.mission.discodeit.repository.file;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileUserStatusRepository implements UserStatusRepository {
    private Map<UUID, UserStatus> data = new ConcurrentHashMap<>();
    private Map<UUID, UserStatus> data_at = new ConcurrentHashMap<>();
    private final String fileDirectory;
    private final String filePath;

    public FileUserStatusRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        this.fileDirectory = fileDirectory;
        this.filePath = fileDirectory + "UserStatus.ser";
        new File(fileDirectory).mkdirs(); // 디렉토리 없으면 생성
        loadFromFile();
    }

    private void saveToFile() {
        File change = new File(filePath);
        File temp = new File(filePath+".temp");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
            oos.writeObject(data);
            temp.renameTo(change);
        } catch (IOException e) {
            temp.delete();
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            this.data = new ConcurrentHashMap<>((Map<UUID, UserStatus>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserStatus create(UserStatus userStatus){
        data.put(userStatus.getUserId(), userStatus);
        saveToFile();
        return userStatus;
    }

    @Override
    public UserStatus readByUserId(UUID userId){
        return data.get(userId);
    }

    @Override
    public List<UserStatus> readAll(){
        return new ArrayList<>(data.values());

    }

    @Override
    public UserStatus update(UUID userId, Instant lastOnlineAt){
        UserStatus userStatus = data.get(userId);
        if(userStatus==null){
            throw new IllegalArgumentException("존재하지 않는 UserStatus입니다.");
        }
        userStatus.updateLastOnlineAt(lastOnlineAt);
        saveToFile();
        return userStatus;
    }

    @Override
    public void delete(UUID userId){
        data_at.put(userId, data.get(userId));
        data.remove(userId);
        saveToFile();
    }

    @Override
    public void restore(UUID userId) {
        // data_at에서 복구
        if (data_at == null || data_at.get(userId) == null) {
            throw new IllegalArgumentException("복구할 UserStatus가 없습니다.");
        }
        data.put(userId, data_at.get(userId));
        data_at.remove(userId);
        saveToFile();
    }
}
