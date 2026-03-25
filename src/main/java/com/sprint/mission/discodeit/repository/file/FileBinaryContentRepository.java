package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileBinaryContentRepository implements BinaryContentRepository {
    private final String CONTENT_FILE = "binaryContents.ser";
    private final File file;
    private final Map<UUID, BinaryContent> binaryContentMap;

    public FileBinaryContentRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        File dir = new File(fileDirectory);

        if(!dir.exists()) {
            dir.mkdirs();
        }

        this.file = new File(dir, CONTENT_FILE);

        System.err.println("📍 [실제 파일 저장 경로] : " + this.file.getAbsolutePath());

        this.binaryContentMap = loadContents();
    }

    private Map<UUID, BinaryContent> loadContents() {

        if(!file.exists()) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, BinaryContent>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("콘텐츠 로드실패", e);
        }
    }

    private void saveContents() {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(binaryContentMap);
        } catch (IOException e) {
            throw new RuntimeException("콘텐츠 저장 실패", e);
        }
    }

    @Override
    public BinaryContent save(BinaryContent content) {
        binaryContentMap.put(content.getId(), content);
        saveContents();
        return content;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return binaryContentMap.values().stream()
                .filter(content -> content.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<BinaryContent> findAllById(Collection<UUID> ids) {
        return binaryContentMap.values().stream()
                .filter(content -> ids.contains(content.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        if(binaryContentMap.remove(id) != null) {
            saveContents();
        } else {
            throw new IllegalArgumentException("존재하지 않는 콘텐츠입니다.");
        }
    }

    @Override
    public void deleteAllByMessageId(UUID id) {
        boolean removed = binaryContentMap.values()
                .removeIf(content -> content.getMessageId().equals(id));

        if(removed) {
            saveContents();
        }
    }
}
