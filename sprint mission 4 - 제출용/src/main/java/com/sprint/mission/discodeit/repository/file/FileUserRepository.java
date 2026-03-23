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
    private Map<UUID, User> data;
    private Map<UUID, User> data_at;
    private final String fileDirectory;
    private final String filePath;

    public FileUserRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        this.fileDirectory = fileDirectory;
        this.filePath = fileDirectory + "users.ser";
        new File(fileDirectory).mkdirs(); // 디렉토리 없으면 생성
        this.data = new HashMap<>();
        loadFromFile();
    }

    private void saveToFile(){
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
    private void loadFromFile(){
        File file = new File(filePath);
        if (!file.exists()) return;

        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            this.data = (Map<UUID, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User create(User user) {
        data.put(user.getId(), user);
        saveToFile();
        return user;
    }

    @Override
    public User read(UUID id) {
        return data.get(id);
    }

    @Override
    public List<User> readAll(){
        return new ArrayList<>(data.values());
    }


    @Override
    public void delete(UUID id) {
        data_at = new HashMap<>();
        data_at.put(id, data.get(id));
        data.remove(id);
        saveToFile();
    }

    @Override
    public User update(User user) {
        saveToFile();  // create랑 동일한 로직
        return user;
    }

    @Override
    public void restore(UUID id) {
        if(data_at == null || data_at.get(id) == null){
            throw new IllegalArgumentException("복구할 데이터가 없습니다.");
        }
        data.put(id, data_at.get(id));
        data_at.remove(id);
        saveToFile();
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
