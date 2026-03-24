package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.userdto.*;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserInfoDto> createUser(@RequestBody CreateUserDto createUserDto){


        UserInfoDto userInfo = userService.create(createUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userInfo);

    }

    @RequestMapping(value = "/{userId}",method = RequestMethod.GET)
    public ResponseEntity<UserInfoDto> readUser(@PathVariable("userId") UUID userId){
        return ResponseEntity.status(HttpStatus.OK).body(userService.find(userId));
    }

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> readAllUser(){

        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    @PutMapping
    public ResponseEntity<UserInfoDto> updateUser(@RequestBody UpdateUserDto updateUserDto){
        System.out.println(updateUserDto);
        UserInfoDto userInfo = userService.updateUser(updateUserDto);
        return ResponseEntity.status(HttpStatus.OK).body(userInfo);
    }



    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestBody DeleteUserDto deleteUserDto){

        userService.delete(deleteUserDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
