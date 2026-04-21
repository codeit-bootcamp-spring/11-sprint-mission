package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.service.dto.binarycontent.CreateBinaryContentRequest;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Transactional
    public BinaryContentDto create(CreateBinaryContentRequest request) {
        validateCreateRequest(request);

        BinaryContent binaryContent = new BinaryContent(
                request.data(),
                request.fileName(),
                request.contentType()
        );
        binaryContentStorage.put(binaryContent.getId(), request.data());
        return binaryContentMapper.toDto(binaryContentRepository.save(binaryContent));
    }

    public BinaryContentDto find(UUID id) {
        return binaryContentMapper.toDto(findEntity(id));
    }

    public BinaryContent findEntity(UUID id) {
        if (id == null) {
            throw new DiscodeitException(ErrorCode.BINARY_CONTENT_ID_REQUIRED);
        }
        return binaryContentRepository.findById(id)
                .orElseThrow(() -> new DiscodeitException(ErrorCode.BINARY_CONTENT_NOT_FOUND));
    }

    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return binaryContentRepository.findAllByIdIn(ids).stream()
                .map(binaryContentMapper::toDto)
                .toList();
    }

    public ResponseEntity<byte[]> downloadAll(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "다운로드할 첨부파일 ID 목록이 비어있어요.");
        }

        List<BinaryContentDto> dtos = findAllByIdIn(ids);
        if (dtos.isEmpty()) {
            throw new DiscodeitException(ErrorCode.BINARY_CONTENT_NOT_FOUND);
        }

        byte[] zipBytes = toZipBytes(dtos);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"binary-contents.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipBytes);
    }

    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new DiscodeitException(ErrorCode.BINARY_CONTENT_ID_REQUIRED);
        }
        BinaryContent binaryContent = binaryContentRepository.findById(id)
                .orElseThrow(() -> new DiscodeitException(ErrorCode.BINARY_CONTENT_NOT_FOUND));
        binaryContentRepository.delete(binaryContent);
    }

    private void validateCreateRequest(CreateBinaryContentRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "첨부파일 생성 요청값이 비어있어요.");
        }
    }

    private byte[] toZipBytes(List<BinaryContentDto> dtos) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            Map<String, Integer> fileNameCounter = new HashMap<>();
            for (BinaryContentDto dto : dtos) {
                String baseName = resolveFileName(dto.fileName(), dto.id());
                String entryName = resolveUniqueFileName(baseName, fileNameCounter);
                zos.putNextEntry(new ZipEntry(entryName));
                try (InputStream inputStream = binaryContentStorage.get(dto.id())) {
                    inputStream.transferTo(zos);
                }
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new DiscodeitException(ErrorCode.INTERNAL_SERVER_ERROR, "바이너리 파일 압축 중 오류가 발생했어요.");
        }
    }

    private String resolveFileName(String fileName, UUID fallbackId) {
        if (fileName == null || fileName.isBlank()) {
            return fallbackId + ".bin";
        }
        return fileName;
    }

    private String resolveUniqueFileName(String fileName, Map<String, Integer> fileNameCounter) {
        int count = fileNameCounter.getOrDefault(fileName, 0);
        fileNameCounter.put(fileName, count + 1);
        if (count == 0) {
            return fileName;
        }
        return "(" + count + ")_" + fileName;
    }
}
