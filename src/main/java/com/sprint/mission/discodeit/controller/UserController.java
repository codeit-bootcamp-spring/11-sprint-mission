package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.dto.userdto.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userdto.request.UserCreateRequest;
import com.sprint.mission.discodeit.service.UserService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {


  private final UserService userService;


  @PostMapping(consumes = "multipart/form-data")
  @ApiResponse(description = "유저 생성", responseCode = "201")
  public ResponseEntity<UserDto> createUser(@Valid @RequestPart UserCreateRequest userCreateRequest,
      @RequestPart(required = false) MultipartFile profile) {

    UserDto userInfo = userService.create(userCreateRequest, profile);

    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(userInfo.id())
        .toUri();

    return ResponseEntity.status(HttpStatus.CREATED).body(userInfo);

  }


  @GetMapping(value = "/{userId}")
  @ApiResponse(description = "유저 1명 정보 가져오기", responseCode = "200")
  public ResponseEntity<UserDto> readUser(@PathVariable("userId") UUID userId) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.find(userId));
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> readAllUser() {

    return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
  }


  @ApiResponse(responseCode = "200")
  @PatchMapping(value = "/{userId}", consumes = "multipart/form-data")
  public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId,
      @Valid @RequestPart UserUpdateRequest userUpdateRequest,
      @RequestPart(required = false) MultipartFile profile) {

    UserDto userInfo = userService.updateUser(userId, userUpdateRequest, profile);
    return ResponseEntity.status(HttpStatus.OK).body(userInfo);
  }


  //멤버 삭제
  @ApiResponse(responseCode = "204", description = "멤버 삭제")
  @DeleteMapping(value = "/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }


}
