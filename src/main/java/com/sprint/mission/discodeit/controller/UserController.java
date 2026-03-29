package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.*;
import com.sprint.mission.discodeit.dto.userstatus.UpdateUserStatusByUserIdResponseDTO;
import com.sprint.mission.discodeit.response.ApiResponse;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    // 심화 요구사항
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAllByUserDto() {
        return ResponseEntity.ok(userService.findAllUserDtos());
    }

    // 사용자 등록
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ApiResponse<SignUpResponseDTO> add(
            @Valid @RequestBody SignUpRequestDTO dto
    ) {
        return ApiResponse.success(userService.signUp(dto));
    }

    // 사용자 정보를 수정 - 사용자를 새로운 사용자로 완전히 대체가 아닌 정보를 수정하는 것이기 때문에 PATCH 사용
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ApiResponse<UpdateUserInfoResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserInfoRequestDTO dto
    ) {
        return ApiResponse.success(userService.updateUserInfo(id, dto));
    }

    // 사용자를 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ApiResponse<Void> delete(
            @PathVariable UUID id
    ) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    // 모든 사용자를 조회
    @RequestMapping(method = RequestMethod.GET)
    public ApiResponse<FindAllUserResponseDTO> findAll() {
        return ApiResponse.success(userService.findAllUser());
    }

    // 사용자의 온라인 상태를 업데이트 - 단순 업데이트 PATCH
    @RequestMapping(value = "/{id}/status", method = RequestMethod.PATCH)
    public ApiResponse<UpdateUserStatusByUserIdResponseDTO> updateUserStatus(
            @PathVariable UUID id
    ) {
        return ApiResponse.success(userStatusService.updateByUserId(id));
    }
}
