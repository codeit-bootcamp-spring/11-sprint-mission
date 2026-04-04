package com.sprint.mission.discodeit.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice // 모든 컨트롤러에서 발생하는 예외를 가로채는 역할
public class GlobalExceptionHandler {
    // 기존 IllegalArgumentException, NoSuchElementException 예외

    // IllegalArgumentException 불법인수, 잘못된 인수(잘못된 요청 ?) / bad request / 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    // NoSuchElementException 찾을수 없는 원소 ?, 해당 원소가 없음 / not found / 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }


    // 추가 : 개발자마저 인지하지 못하는 예외가 있을 수 있기 때문에 예외 최상위 클래스 Exception 예외를 추가, 500번(예외 발생, 서버 코드 문제, 개발자 실수)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOtherException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

}
