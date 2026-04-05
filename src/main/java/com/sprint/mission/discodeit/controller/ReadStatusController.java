package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "ReadStatus")
@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @Operation(summary = "Message 읽음 상태 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Message 읽음 상태가 성공적으로 생성됨"),
            @ApiResponse(responseCode = "400", description = "이미 읽음 상태가 존재함"),
            @ApiResponse(responseCode = "404", description = "Channel 또는 User를 찾을 수 없음")
    })
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatusDto> create(@RequestBody ReadStatusCreateRequest dto) {
        ReadStatusDto result = readStatusService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Message 읽음 상태 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message 읽음 상태가 성공적으로 수정됨"),
            @ApiResponse(responseCode = "404", description = "Message 읽음 상태를 찾을 수 없음")
    })
    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
    public ResponseEntity<Void> update(
            @Parameter(description = "수정할 읽음 상태 ID")
            @PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest dto
    ) {
        readStatusService.update(readStatusId, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "User의 Message 읽음 상태 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message 읽음 상태 목록 조회 성공")
    })
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatusDto>> findAllByUserId(
            @Parameter(description = "조회할 User ID")
            @RequestParam UUID userId
    ) {
        List<ReadStatusDto> result = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(result);
    }

}
