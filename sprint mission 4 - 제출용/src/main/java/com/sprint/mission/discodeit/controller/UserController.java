package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserStatusService userStatusService;

    // create
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserResponse> create(@RequestBody UserCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    // read
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public ResponseEntity<UserResponse> read(@PathVariable UUID id){
        return ResponseEntity.ok(userService.read(id));
    }

    // readAll
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<UserResponse>> readAll(){
        return ResponseEntity.ok(userService.readAll());
    }

    // update
    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    public ResponseEntity<Void> update(@PathVariable UUID id,
                                       @RequestBody UserUpdateRequest request){
        userService.update(request);
        return ResponseEntity.ok().build();
    }

    // delete
    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // restore
    @RequestMapping(value = "/{id}/", method = RequestMethod.POST)
    public ResponseEntity<Void> restore(@PathVariable UUID id){
        userService.restore(id);
        return ResponseEntity.ok().build();
    }

    // UserUpdate
    @RequestMapping(value = "/{id}/status", method = RequestMethod.PUT)
    public ResponseEntity<Void> updateStatus(@PathVariable UUID id, @RequestBody UserStatusUpdateRequest request){
        userStatusService.update(request);
        return ResponseEntity.ok().build();
    }
}
