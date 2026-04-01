package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileBinaryContentRepository implements BinaryContentRepository {
    private final String CONTENT_FILE = "binaryContents.ser";
    private final File dir;
    private final File metadataFile;
    private final Map<UUID, BinaryContent> binaryContentMap;

    public FileBinaryContentRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        this.dir = new File(fileDirectory);

        if(!this.dir.exists()) {
            boolean isCreated = this.dir.mkdirs();

            if(!isCreated) {
                throw new IllegalArgumentException("⚠️ 파일 저장 디렉토리를 생성할 수 없습니다.");
            }
        }

        this.metadataFile = new File(this.dir, CONTENT_FILE);

        this.binaryContentMap = loadMetadata();
    }

    private Map<UUID, BinaryContent> loadMetadata() {

        if(!metadataFile.exists()) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metadataFile))) {
            return (Map<UUID, BinaryContent>) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("데이터 로드 실패", e);
        }
    }

    private void saveMetadata() {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(metadataFile))) {
            oos.writeObject(binaryContentMap);
        } catch (IOException e) {
            throw new RuntimeException("메타데이터 저장 실패", e);
        }
    }

    @Override
    public BinaryContent save(BinaryContent content) {
        if(content.getData() != null) {
            File actualFile = new File(dir, content.getId().toString() + ".bin");
            try (FileOutputStream fos = new FileOutputStream(actualFile)) {
                fos.write(content.getData());
            } catch (IOException e) {
                throw new RuntimeException("실제 파일 저장 실패", e);
            }
        }
        binaryContentMap.put(content.getId(), content);
        saveMetadata();
        return content;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        BinaryContent content = binaryContentMap.get(id);
        if(content != null) {
            File actualFile = new File(dir, id.toString() + ".bin");
            if(actualFile.exists()) {
                try {
                    content.setData(Files.readAllBytes(actualFile.toPath()));
                } catch (IOException e) {
                    throw new RuntimeException("파일 읽기 실패", e);
                }
            }
            return Optional.of(content);
        }
        return Optional.empty();
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
            saveMetadata();

            File actualFile = new File(dir, id.toString() + ".bin");
            if(actualFile.exists()) {
                boolean isDeleted = actualFile.delete();

                if(!isDeleted) {
                    System.err.println("⚠️ 경고 : 실제 파일 삭제에 실패했습니다. (경로 : " + actualFile.getAbsolutePath() + ")");
                }
            }
        } else {
            throw new IllegalArgumentException("존재하지 않는 콘텐츠입니다.");
        }
    }

    @Override
    public void deleteAllByAttachmentIds(List<UUID> attachmentIds) {
        if(attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }

        for(UUID id : attachmentIds) {
            BinaryContent content = binaryContentMap.remove(id);

            if(content != null) {
                File actualFile = new File(dir, id.toString() + ".bin");
                if(actualFile.exists()) {
                    if(!actualFile.delete()) {
                        System.out.println("⚠️ 실제 파일 삭제 실패 (경로 : " + actualFile.getAbsolutePath() + ")");
                    }
                }
            }
        }
        saveMetadata();
    }
}
