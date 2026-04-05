package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.FileResponseDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.FileMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContent")
@RequiredArgsConstructor
public class FileMessageController {
    private final FileMessageService fileMessageService;

    // 1. 단일 바이너리 파일 조회( ex: /files/{fileId})
    @RequestMapping(value = "/{fileId}", method = RequestMethod.GET)
    public FileResponseDto getFile(@PathVariable UUID fileId) {
        return fileMessageService.findById(fileId);
    }

    // 2. 다중 바이너리 파일 조회( ex: /files?ids=xxx,yyy,zzz)
    @RequestMapping(method = RequestMethod.GET)
    public List<FileResponseDto> getFiles(@RequestParam List<UUID> ids) {
        return fileMessageService.findAllByIds(ids);
    }

    @GetMapping("/find")
    public ResponseEntity<BinaryContent> findBinaryContent(@RequestParam UUID binaryContentId) {
        // 실제 서비스 호출 대신, 가짜(BinaryContent) 객체 직접 생성
        BinaryContent content = new BinaryContent(
                "text.png", //파일이름
                new byte[]{0x10, 0x20, 0x30, 0x40},
                "image/png",    //contentType 예시
                UUID.randomUUID(),        // userId
                binaryContentId           //messageId 등 필요한 값 (테스트용)
        );
        return ResponseEntity.ok(content);
    }
}
