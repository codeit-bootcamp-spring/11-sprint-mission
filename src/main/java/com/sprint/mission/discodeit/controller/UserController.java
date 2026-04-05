package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<UserDto>> findAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // 1. 사용자 등록
    @RequestMapping(method = RequestMethod.POST)
    public UserResponseDto createUser(@RequestBody UserCreateRequestDto requestDto) {
        return userService.create(requestDto);
    }

    // 2. 사용자 정보 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public UserResponseDto updateUser(
            @PathVariable UUID id,
            @RequestBody UserUpdateRequestDto updateRequestDto) {
        return userService.update(id, updateRequestDto);
    }

    // 3. 사용자 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable UUID id) {
        userService.delete(id);
    }

    // 4. 모든 사용자 조회
    @RequestMapping(method = RequestMethod.GET)
    public List<UserResponseDto> getAllUsers() {
        return userService.findAll();
    }

    // 5. 사용자의 온라인 상태 업데이트
    @RequestMapping(value = "/{id}/online", method = RequestMethod.PATCH)
    public UserStatusResponseDto updateOnlineStatus(@PathVariable UUID id, @RequestBody UserStatusResponseDto statusUpdateRequestDto) {
        return userService.updateOnlineStatus(id, statusUpdateRequestDto);
    }

    // 6. 사용자 로그인 검증
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto) {
        // 실제로는 userService.login(loginRequestDto) 등으로 검증!
        boolean isSuccess = userService.login(loginRequestDto);

        if (isSuccess) {
            return new LoginResponseDto("로그인 성공!");
        } else {
            return new LoginResponseDto("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

}
