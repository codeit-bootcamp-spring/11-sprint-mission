package com.sprint.mission.discodeit.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.FileMessage;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Repository
public class FileMessageRepositoryImpl implements FileMessageRepository {
    private static final String UPLOAD_DIR = "./uploads/"; // 저장 경로

    private final ObjectMapper objectMapper = new ObjectMapper();

    public FileMessageRepositoryImpl() {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    @Override
    public Optional<FileMessage> findById(UUID id) {
        File dataFile = new File(UPLOAD_DIR + id + ".data");
        File metaFile = new File(UPLOAD_DIR + id + ".meta");
        if (!dataFile.exists() || !metaFile.exists()) return Optional.empty();

        try {
            byte[] content = Files.readAllBytes(dataFile.toPath());
            Map<String, Object> meta = objectMapper.readValue(metaFile, Map.class);

            FileMessage file = new FileMessage();
            file.setId(id);
            file.setFileName((String) meta.get("fileName"));
            file.setContentType((String) meta.get("contentType"));
            file.setContent(content);
            return Optional.of(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FileMessage> findAllByIds(List<UUID> ids) {
        List<FileMessage> list = new ArrayList<>();
        for (UUID id : ids) {
            findById(id).ifPresent(list::add);
        }
        return list;
    }

    @Override
    public void save(FileMessage fileMessage) {
        // 내용 저장
        try {
            Files.write(Paths.get(UPLOAD_DIR + fileMessage.getId() + ".data"), fileMessage.getContent());

            // 메타정보(JSON) 저장
            Map<String, Object> meta = new HashMap<>();
            meta.put("fileName", fileMessage.getFileName());
            meta.put("contentType", fileMessage.getContentType());
            objectMapper.writeValue(new File(UPLOAD_DIR + fileMessage.getId() + ".meta"), meta);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        new File(UPLOAD_DIR + id + ".data").delete();
        new File(UPLOAD_DIR + id + ".meta").delete();
    }
}