package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponseDTO;
import com.sprint.mission.discodeit.dto.binarycontent.CreateBinaryContentRequestDTO;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.response.ApiResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    // 심화 요구사항
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> find(
            @RequestParam("binaryContentId") UUID binaryContentId
    ) {
        BinaryContent content = binaryContentService.find(binaryContentId);
        return ResponseEntity.ok(content);
    }

    // 파일 업로드 API
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<BinaryContentResponseDTO> upload(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        CreateBinaryContentRequestDTO dto = new CreateBinaryContentRequestDTO(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );

        return ApiResponse.success(BinaryContentResponseDTO.from(binaryContentService.create(dto)));
    }

    // 단건 조회
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ApiResponse<BinaryContentResponseDTO> findContent(
            @PathVariable UUID id
    ) {
        BinaryContentResponseDTO responseDto = BinaryContentResponseDTO.from(binaryContentService.find(id));
        return ApiResponse.success(responseDto);
    }

    // 다건 조회
    @RequestMapping(method = RequestMethod.GET)
    public ApiResponse<List<BinaryContentResponseDTO>> findMultipleContents(
            @RequestParam List<UUID> ids
    ) {
        List<BinaryContentResponseDTO> responseList = binaryContentService.findAllByIdIn(ids).stream()
                .map(BinaryContentResponseDTO::from)
                .toList();

        return ApiResponse.success(responseList);
    }
}
