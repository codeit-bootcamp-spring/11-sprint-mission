package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.UserStatusDto;
import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "UserStatus", description = "User 상태 API")
public interface UserStatusApi {

    @Operation(summary = "User 상태 생성")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User 상태가 성공적으로 생성됨",
                    content = @Content(schema = @Schema(implementation = UserStatusDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "User with id {userId} not found"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 User 상태가 존재함",
                    content = @Content(examples = @ExampleObject(value = "UserStatus with userId {userId} already exists"))
            )
    })
    ResponseEntity<UserStatusDto> create(
            @Parameter(description = "User 상태 생성 정보") UserStatusCreateRequest request
    );

    @Operation(summary = "User 상태 단건 조회")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserStatusDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User 상태를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "UserStatus with id {userStatusId} not found"))
            )
    })
    ResponseEntity<UserStatusDto> find(
            @Parameter(description = "조회할 UserStatus ID") UUID userStatusId
    );

    @Operation(summary = "User 상태 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User 상태 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserStatusDto.class)))
            )
    })
    ResponseEntity<List<UserStatusDto>> findAll();

    @Operation(summary = "User 상태 수정")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User 상태가 성공적으로 수정됨",
                    content = @Content(schema = @Schema(implementation = UserStatusDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User 상태를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "UserStatus with id {userStatusId} not found"))
            )
    })
    ResponseEntity<UserStatusDto> update(
            @Parameter(description = "수정할 UserStatus ID") UUID userStatusId,
            @Parameter(description = "수정할 User 상태 정보") UserStatusUpdateRequest request
    );

    @Operation(summary = "User 상태 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User 상태가 성공적으로 삭제됨"),
            @ApiResponse(
                    responseCode = "404",
                    description = "User 상태를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "UserStatus with id {userStatusId} not found"))
            )
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "삭제할 UserStatus ID") UUID userStatusId
    );
}