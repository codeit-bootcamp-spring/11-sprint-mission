package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
// application.yaml의 설정값에 따라 Bean을 설정 / name : 설정값의 이름, havingValue : type 지정
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileReadStatusRepository implements ReadStatusRepository {
    // ReadStatus들을 담을 Map 생성
    private final Map<UUID, ReadStatus> readStatuses = new HashMap<>(); // 저장소

    private final String fileDirectory;
    private final String fileName = "/readStatus.ser";
    private final File file;

    public FileReadStatusRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        this.fileDirectory = fileDirectory;
        this.file = new File(fileDirectory + fileName);
        load();
    }

    // 저장 메서드 save(직렬화)
    private void save() {
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(readStatuses);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 불러오기 메서드 load(역직렬화)
    private void load() {
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Map<UUID, ReadStatus> loadReadStatuses = (Map<UUID, ReadStatus>) ois.readObject();
            readStatuses.clear(); // 한 번 비우고
            readStatuses.putAll(loadReadStatuses); // 불러온다.(기존에 있던 데이터까지 같이 로드될 수 있기 때문에)
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void insert(ReadStatus readStatus) {
        readStatuses.put(readStatus.getId(), readStatus);
        save();
    }

    @Override
    public ReadStatus findById(UUID id) {
        ReadStatus readStatus = readStatuses.get(id);
        if (readStatus == null) {
            throw new NoSuchElementException("해당 ReadStatus가 존재하지 않습니다. id : " + id);
        }

        return readStatus;
    }

    @Override
    public List<ReadStatus> findByUserId(UUID userId) {
        return readStatuses.values().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReadStatus> findByChannelId(UUID channelId) {
        return readStatuses.values().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }

    @Override
    public void update(ReadStatus readStatus) {
        readStatuses.put(readStatus.getId(), readStatus);
        save();
    }

    @Override
    public void delete(UUID id) {
        readStatuses.remove(id);
        save();
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        readStatuses.values().removeIf(readStatus -> readStatus.getChannelId().equals(channelId));
        save();
    }
}
