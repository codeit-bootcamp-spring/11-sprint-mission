package com.sprint.mission.discodeit.dto.binarycontent;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public record BinaryContentCreateRequest(
        byte[] data,
        String fileName,
        String contentType,
        long size
) {
    public static Optional<BinaryContentCreateRequest> from(MultipartFile profile) {
        if (profile == null || profile.isEmpty()) {
            return Optional.empty();
        } else {
            try {
                BinaryContentCreateRequest binaryContentCreateRequest = new BinaryContentCreateRequest(
                        profile.getBytes(),
                        profile.getOriginalFilename(),
                        profile.getContentType(),
                        profile.getSize()
                );
                return Optional.of(binaryContentCreateRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<BinaryContentCreateRequest> listFrom(List<MultipartFile> attachments) {
        return attachments.stream()
                .flatMap(attachment -> BinaryContentCreateRequest.from(attachment).stream())
                .toList();
    }
}
