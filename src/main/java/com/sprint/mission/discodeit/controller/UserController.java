package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserReadDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// 사용자 관리 컨트롤러
@RestController // Json 반환을 위해 @Controller 대신 @ResponseBody를 포함한 @RestController 사용
@RequestMapping(EndPoints.USER)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService; // 특정 유저 생성, 모든 유저 조회, 특정 유저 업데이트, 특정 유저 삭제
    private final UserStatusService userStatusService; // 특정 유저의 상태 업데이트

    // 특정 사용자 등록
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<User> create(@RequestBody UserCreateRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    // 특정 사용자 정보 수정
    @RequestMapping(value = "/{user-id}", method = RequestMethod.PUT)
    public ResponseEntity<User> update(@PathVariable("user-id") UUID id, @RequestBody UserUpdateRequest dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    // 특정 사용자 삭제
    @RequestMapping(value = "/{user-id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable("user-id") UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 모든 사용자 조회, 심화 요구사항 추가
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserReadDto>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }
//    @RequestMapping(method = RequestMethod.GET)
//    public ResponseEntity<List<UserReadDto>> readAll() {
//        return ResponseEntity.ok(userService.findAll());
//    }

    // 특정 사용자의 상태(온/오프라인) 업데이트
    @RequestMapping(value = "/{user-id}/status", method = RequestMethod.PUT)
    public ResponseEntity<UserStatus> updateStatus(@PathVariable("user-id") UUID id) {
        return ResponseEntity.ok(userStatusService.updateByUserId(id));
    }


}
