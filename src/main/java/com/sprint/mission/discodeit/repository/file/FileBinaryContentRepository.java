package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
@Profile("repo-file")
public class FileBinaryContentRepository implements BinaryContentRepository {

    private final Path directory;

    public FileBinaryContentRepository(
            @Value("${discodeit.repository.file-directory}") String baseDirectory
    ) {
        this.directory = Paths.get(baseDirectory, "binary-contents");
        initDirectory(this.directory);
    }

    private void initDirectory(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException("BinaryContent 폴더 생성 중 오류가 발생했습니다.", e);
            }
        }
    }

    @Override
    public void save(BinaryContent binaryContent) {
        Path filePath = directory.resolve(binaryContent.getId().toString().concat(".ser"));
        try (
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(binaryContent);
        } catch (IOException e) {
            throw new RuntimeException("BinaryContent 파일 저장 실패.", e);
        }
    }

    @Override
    public BinaryContent findById(UUID id) {
        Path filePath = directory.resolve(id.toString().concat(".ser"));
        if (!Files.exists(filePath)) {
            return null;
        }

        try (
                FileInputStream fis = new FileInputStream(filePath.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            BinaryContent content = (BinaryContent) ois.readObject();
            if (content.isDeleted()) {
                return null;
            }
            return content;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("BinaryContent 파일 불러오기 실패.", e);
        }
    }

    @Override
    public List<BinaryContent> findAll() {
        if (!Files.exists(directory)) return new ArrayList<>();

        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .filter(path -> path.toString().endsWith(".ser"))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (BinaryContent) ois.readObject();
                        } catch (Exception e) {
                            throw new RuntimeException("개별 BinaryContent 파일 읽기 실패", e);
                        }
                    })
                    .filter(content -> !content.isDeleted())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("BinaryContent 목록 조회 중 오류 발생", e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        BinaryContent content = findById(id);
        if (content != null) {
            content.softDelete();
            save(content);
        }
    }
}