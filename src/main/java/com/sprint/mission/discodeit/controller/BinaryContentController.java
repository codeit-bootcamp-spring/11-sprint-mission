package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController // Json 반환을 위해 @Controller 대신 @ResponseBody를 포함한 @RestController 사용
@RequestMapping(EndPoints.BINARY_CONTENT)
@RequiredArgsConstructor
public class BinaryContentController {
    private final BinaryContentService binaryContentService;

    // 바이너리 파일을 1개만 조회(api/binaryContents/find?binaryContentId=...), 심화 요구사항 추가
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> find(@RequestParam UUID binaryContentId) {
        return ResponseEntity.ok(binaryContentService.find(binaryContentId));
    }

    // 바이너리 파일을 1개 또는 여러 개 조회(api/binaryContents/find?binaryContentId=...&binaryContentId=...), 심화 요구사항 추가
    @RequestMapping(value = "/findMany", method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContent>> findMany(@RequestParam List<UUID> binaryContentId) {
        return ResponseEntity.ok(binaryContentService.findAllByIdIn(binaryContentId));
    }
}
