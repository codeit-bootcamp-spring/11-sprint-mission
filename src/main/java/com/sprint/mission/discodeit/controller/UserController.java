package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.error.ExceptionDto;
import com.sprint.mission.discodeit.dto.userdto.*;
import com.sprint.mission.discodeit.dto.userstatusdto.CreateUserStatusDto;
import com.sprint.mission.discodeit.dto.userstatusdto.UserStatusInfoDto;
import com.sprint.mission.discodeit.exception.service.DiffPasswordException;
import com.sprint.mission.discodeit.exception.service.DupEmailException;
import com.sprint.mission.discodeit.exception.service.DupNameException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserInfoDto> createUser(@ModelAttribute CreateUserDto createUserDto){


        UserInfoDto userInfo = userService.create(createUserDto);


        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userInfo.userId())
                .toUri();



        return ResponseEntity.created(uri).body(userInfo);

    }

    @RequestMapping(value = "/{userId}",method = RequestMethod.GET)
    public ResponseEntity<UserInfoDto> readUser(@PathVariable("userId") UUID userId){
        return ResponseEntity.status(HttpStatus.OK).body(userService.find(userId));
    }

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> readAllUser(){

        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<UserInfoDto> updateUser(@RequestBody UpdateUserDto updateUserDto){
        System.out.println(updateUserDto);
        UserInfoDto userInfo = userService.updateUser(updateUserDto);
        return ResponseEntity.status(HttpStatus.OK).body(userInfo);
    }

    @RequestMapping(value = "/{userId}/status", method = RequestMethod.PUT)
    public ResponseEntity<UserInfoDto> updateUserStatus(@PathVariable UUID userId){

        userStatusService.update(new CreateUserStatusDto(userId));

        return ResponseEntity.status(HttpStatus.OK).body(userService.find(userId));

    }



    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteUser(@RequestBody DeleteUserDto deleteUserDto){

        userService.delete(deleteUserDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



    @ExceptionHandler(DiffPasswordException.class)
    public ResponseEntity<ExceptionDto> diffPasswordHandler(DiffPasswordException e, HttpServletRequest request){


        ExceptionDto exceptionDto = ExceptionDto.of(
                HttpStatus.UNAUTHORIZED,
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(401).body(exceptionDto);

    }

    @ExceptionHandler(DupNameException.class)
    public ResponseEntity<ExceptionDto> dupNameHandler(DupNameException e, HttpServletRequest request){

        ExceptionDto exceptionDto = ExceptionDto.of(
                HttpStatus.CONFLICT,
                e.getMessage(),
                request.getRequestURI()
        );


        return ResponseEntity.status(409).body(exceptionDto);
    }


    @ExceptionHandler(DupEmailException.class)
        public ResponseEntity<ExceptionDto> dupEmailHandler(DupEmailException e, HttpServletRequest request){

            ExceptionDto exceptionDto = ExceptionDto.of(
                    HttpStatus.CONFLICT,
                    e.getMessage(),
                    request.getRequestURI()
            );


            return ResponseEntity.status(409).body(exceptionDto);

    }



}
