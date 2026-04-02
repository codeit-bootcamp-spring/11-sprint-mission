package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.error.ExceptionDto;
import com.sprint.mission.discodeit.exception.service.AlreadyExistException;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(AlreadyExistException.class)

    public ResponseEntity<ExceptionDto> alreadyExistHandler(AlreadyExistException e, HttpServletRequest request){
        ExceptionDto exceptionDto = ExceptionDto.of(
                HttpStatus.CONFLICT,
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(409).body(exceptionDto);
    }
    @ExceptionHandler(NonExistException.class)

    public ResponseEntity<ExceptionDto> NonExistHandler(NonExistException e, HttpServletRequest request){


        ExceptionDto exceptionDto = ExceptionDto.of(
                HttpStatus.NOT_FOUND,
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(exceptionDto.code()).body(exceptionDto);
    }









    @ExceptionHandler()
    public ResponseEntity<ExceptionDto>handleException(IllegalArgumentException e, HttpServletRequest request) {

        ExceptionDto exceptionDto = ExceptionDto.of(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(400).body(exceptionDto);
    }




    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDto> handleException(Exception e,HttpServletRequest request) {

        ExceptionDto exceptionDto = ExceptionDto.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(500).body(exceptionDto);
    }


}
